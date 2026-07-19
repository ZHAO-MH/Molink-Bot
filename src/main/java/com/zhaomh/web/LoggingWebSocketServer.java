package com.zhaomh.web;

import com.zhaomh.logger.Logger;
import com.zhaomh.logger.LoggerFactory;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LoggingWebSocketServer {
    private static final Logger log = LoggerFactory.getLogger(LoggingWebSocketServer.class);
    private static final int MAX_CACHE_SIZE = 100;

    private final WebSocketServer server;
    private final ConcurrentLinkedQueue<WebSocket> clients = new ConcurrentLinkedQueue<>();

    // 日志缓存：线程安全的列表，只追加，自动淘汰
    private final List<String> logCache = Collections.synchronizedList(new ArrayList<>());

    private static volatile LoggingWebSocketServer instance;

    public LoggingWebSocketServer(int port) {
        server = new WebSocketServer(new InetSocketAddress(port)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                clients.add(conn);
                log.info("日志客户端已连接: {}", conn.getRemoteSocketAddress());
                // 新连接建立后，立即发送历史日志
                sendHistoryLogs(conn);
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                clients.remove(conn);
                log.info("日志客户端断开: {}", conn.getRemoteSocketAddress());
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                // 忽略客户端消息（可用来做心跳确认）
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                if (conn != null) clients.remove(conn);
                log.error("日志 WebSocket 错误", ex);
            }

            @Override
            public void onStart() {
                log.info("日志 WebSocket 服务已启动，监听端口: {}", port);
            }
        };
        server.setReuseAddr(true);
        instance = this;
    }

    /** 给新客户端发送历史缓存日志（最多 100 条） */
    private void sendHistoryLogs(WebSocket conn) {
        if (!conn.isOpen()) return;
        synchronized (logCache) {
            for (String cached : logCache) {
                if (conn.isOpen()) {
                    conn.send(cached);
                } else {
                    break;
                }
            }
        }
    }

    public void start() {
        server.start();
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.stop();
        }
        instance = null;
    }

    /** 判断服务是否就绪 */
    public static boolean isReady() {
        return instance != null && instance.server != null;
    }

    /** 广播日志 + 写入缓存 */
    public static void broadcast(String level, String message) {
        if (!isReady()) return;

        String json = String.format(
                "{\"level\":\"%s\",\"msg\":\"%s\",\"time\":%d}",
                escape(level),
                escape(message),
                System.currentTimeMillis()
        );

        // 写入缓存
        instance.cacheLog(json);

        // 广播给所有客户端
        for (WebSocket client : instance.clients) {
            if (client.isOpen()) {
                client.send(json);
            }
        }
    }

    /** 缓存单条日志，自动淘汰最旧的 */
    private void cacheLog(String jsonLine) {
        synchronized (logCache) {
            logCache.add(jsonLine);
            if (logCache.size() > MAX_CACHE_SIZE) {
                logCache.removeFirst();
            }
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}