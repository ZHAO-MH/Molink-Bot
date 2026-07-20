/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.zhaomh.core.Accessor;
import com.zhaomh.plugin.PluginManager;
import com.zhaomh.bot.OneBotClient;
import com.zhaomh.command.CommandManager;
import com.zhaomh.logger.Logger;
import com.zhaomh.logger.LoggerFactory;
import com.zhaomh.service.StatusService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

public class WebUiServer {
    private static final Logger log = LoggerFactory.getLogger(WebUiServer.class);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final int port;
    private final Accessor accessor;
    private final PluginManager pluginManager;
    private final OneBotClient client;
    private final CommandManager commandManager;
    private HttpServer server;
    private final long startTime = System.currentTimeMillis();

    public WebUiServer(int port, Accessor accessor, PluginManager pluginManager, OneBotClient client, CommandManager commandManager) {
        this.port = port;
        this.accessor = accessor;
        this.pluginManager = pluginManager;
        this.client = client;
        this.commandManager = commandManager;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newCachedThreadPool());

        // 1. API 端点（返回 JSON 数据）
        server.createContext("/api/status", this::handleStatus);
        server.createContext("/api/plugins", this::handlePlugins);
        server.createContext("/api/commands", this::handleCommands);
        server.createContext("/api/toggle", this::handleToggle);

        // 2. 静态资源（前端页面）
        server.createContext("/", this::handleStatic);

        server.start();
        log.info("Web UI 已启动: http://localhost:{}", port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            log.info("Web UI 已停止");
        }
    }

    // ========== API 处理器 ==========

    private void handleStatus(HttpExchange exchange) throws IOException {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "ok");

        // 获取 StatusService
        StatusService status = accessor.getService(StatusService.class);
        data.put("uptime", status.getUptimeMillis() / 1000);
        data.put("uptimeFormatted", status.getUptimeFormatted());
        data.put("usedMemoryMB", status.getUsedMemoryMB());
        data.put("maxMemoryMB", status.getMaxMemoryMB());
        data.put("wsConnected", status.isBotConnected());
        data.put("registeredCommands", commandManager.getCommandCount());
        data.put("loadedPlugins", pluginManager.getAllPlugins().size());
        data.put("wsUrl", client.getWsUrl());

        respondJson(exchange, data);
    }

    private void handlePlugins(HttpExchange exchange) throws IOException {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Boolean> states = pluginManager.getAllPluginStates();
        for (Map.Entry<String, Boolean> entry : states.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", entry.getKey());
            item.put("enabled", entry.getValue());
            // 判断是否为核心插件
            var plugin = pluginManager.getPluginByName(entry.getKey());
            boolean isCore = plugin != null && plugin.getClass().isAnnotationPresent(com.zhaomh.core.annotation.Core.class);
            item.put("core", isCore);
            list.add(item);
        }

        result.put("plugins", list);
        respondJson(exchange, result);
    }

    private void handleCommands(HttpExchange exchange) throws IOException {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> cmdList = commandManager.getCommandNames();
        result.put("commands", cmdList);
        respondJson(exchange, result);
    }

    private void handleToggle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        // 解析请求参数 /api/toggle?name=PluginName&enable=true
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = parseQuery(query);
        String name = params.get("name");
        String enableStr = params.get("enable");

        if (name == null || enableStr == null) {
            respondJson(exchange, Map.of("error", "Missing name or enable"));
            return;
        }

        boolean enable = Boolean.parseBoolean(enableStr);
        try {
            if (enable) {
                pluginManager.enablePlugin(name);
            } else {
                pluginManager.disablePlugin(name);
            }
            respondJson(exchange, Map.of("success", true, "name", name, "enabled", enable));
        } catch (Exception e) {
            respondJson(exchange, Map.of("error", e.getMessage()));
        }
    }

    // ========== 静态资源托管（内嵌 index.html） ==========

    private void handleStatic(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }

        // 从 classpath 加载 /static/ 下的文件
        InputStream is = getClass().getResourceAsStream("/static" + path);
        if (is == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        String mime = "text/html";
        if (path.endsWith(".css")) mime = "text/css";
        else if (path.endsWith(".js")) mime = "application/javascript";
        else if (path.endsWith(".png")) mime = "image/png";

        exchange.getResponseHeaders().set("Content-Type", mime);
        exchange.sendResponseHeaders(200, 0);

        try (OutputStream os = exchange.getResponseBody()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        }
        is.close();
    }

    // ========== 工具方法 ==========

    private void respondJson(HttpExchange exchange, Object data) throws IOException {
        String json = gson.toJson(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        // 允许跨域（方便开发时调试）
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.sendResponseHeaders(200, json.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                result.put(kv[0], kv[1]);
            }
        }
        return result;
    }
}