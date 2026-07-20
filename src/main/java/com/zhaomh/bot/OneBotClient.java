/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.bot;

import com.google.gson.*;
import com.zhaomh.logger.Logger;
import com.zhaomh.logger.LoggerFactory;
import com.zhaomh.util.JsonUtil;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class OneBotClient {
    private static final Logger log = LoggerFactory.getLogger(OneBotClient.class);
    private final String wsUrl;
    private EventCallback eventcallback;
    private WebSocketClient ws;

    private final Map<String, Consumer<JsonObject>> pendingRequests = new ConcurrentHashMap<>();

    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    // ========== 新增群级别单线程执行器容器 ==========
    // 为每个群 ID 分配一个单线程的 ExecutorService，保证该群内的所有消息串行处理
    // 使用 ConcurrentHashMap 保证线程安全
    private final Map<String, ExecutorService> groupExecutors = new ConcurrentHashMap<>();

    // 公共线程池，用于非群消息（私聊、元事件、API 响应等）的并发处理
    private final ExecutorService messageExecutor = Executors.newCachedThreadPool();

    public OneBotClient(String wsUrl) {
        this.wsUrl = wsUrl;
    }

    public void connect() {
        try {
            ws = new WebSocketClient(new URI(wsUrl)) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    log.info("已连接到 OneBot[{}]", wsUrl);
                }

                // ========== 重写消息接收逻辑，根据群 ID 路由到对应执行器 ==========
                @Override
                public void onMessage(String message) {
                    JsonObject json = JsonUtil.GSON.fromJson(message, JsonObject.class);
                    log.info("收到消息: {}", json);

                    // 解析群 ID（仅群消息需要串行化处理）
                    String groupId = extractGroupId(json);

                    if (groupId != null) {
                        // 群消息：获取或创建该群的单线程执行器，保证该群消息按序处理
                        ExecutorService groupExecutor = groupExecutors.computeIfAbsent(
                                groupId,
                                k -> Executors.newSingleThreadExecutor(r -> {
                                    Thread t = new Thread(r, "group-" + k);
                                    t.setDaemon(true);
                                    return t;
                                })
                        );
                        groupExecutor.submit(() -> dispatchMessage(json));
                    } else {
                        // 非群消息：使用公共线程池快速处理，不影响主线程
                        messageExecutor.submit(() -> dispatchMessage(json));
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.info("连接已关闭: {} {}", code, reason);
                    // ========== 修改点 3：关闭所有群执行器，释放资源 ==========
                    groupExecutors.values().forEach(ExecutorService::shutdownNow);
                    groupExecutors.clear();
                    heartbeatExecutor.shutdown();
                }

                @Override
                public void onError(Exception ex) {
                    log.error("WebSocket 错误: {}", ex.getMessage());
                }
            };
            ws.connectBlocking();
        } catch (URISyntaxException | InterruptedException e) {
            log.error("连接失败", e);
        }
    }

    // ========== 从 JSON 中提取群 ID ==========
    private String extractGroupId(JsonObject json) {
        if (json.has("post_type") && "message".equals(json.get("post_type").getAsString())) {
            if (json.has("message_type") && "group".equals(json.get("message_type").getAsString())) {
                if (json.has("group_id")) {
                    return json.get("group_id").getAsString();
                }
            }
        }
        return null; // 非群消息返回 null
    }

    // ========== 统一的消息分发逻辑（与原来 onMessage 中的处理相同） ==========
    private void dispatchMessage(JsonObject json) {
        try {
            if (json.has("post_type")) {
                eventcallback.handle(json);
            } else if (json.has("echo")) {
                // 处理 API 响应回调
                String echo = json.get("echo").getAsString();
                Consumer<JsonObject> callback = pendingRequests.remove(echo);
                if (callback != null) callback.accept(json);
            }
        } catch (Exception e) {
            log.error("消息处理失败", e);
        }
    }

    // 事件分发
    public void setEventCallback(EventCallback eventCallback) {
        this.eventcallback = eventCallback;
    }

    // 封装 API 调用
    public void callApi(String action, JsonObject params, Consumer<JsonObject> callback) {
        if (ws == null || !ws.isOpen()) {
            log.error("尚未连接");
            return;
        }
        String echo = UUID.randomUUID().toString().substring(0, 6);
        pendingRequests.put(echo, callback);

        JsonObject request = new JsonObject();
        request.addProperty("action", action);
        request.addProperty("echo", echo);
        if (params != null) {
            request.add("params", params);
        }
        log.info("发送请求: {}", request);
        ws.send(JsonUtil.GSON.toJson(request));
    }

    public CompletableFuture<JsonObject> callApiAsync(String action, JsonObject params) {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        callApi(action, params, future::complete);
        return future;
    }

    @FunctionalInterface
    public interface EventCallback {
        void handle(JsonObject json);
    }

    public void close() {
        if (ws != null && ws.isOpen()) {
            ws.close();
            try {
                ws.closeBlocking();
            } catch (InterruptedException e) {
                log.error("关闭连接时出错", e);
            }
        }
    }

    public String getWsUrl() {
        return wsUrl;
    }

    public boolean isConnected() {
        return ws != null && ws.isOpen();
    }
}