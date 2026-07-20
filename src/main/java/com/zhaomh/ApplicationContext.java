/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh;

import com.zhaomh.bot.OneBotClient;
import com.zhaomh.builtin.BuiltinBotPlugin;
import com.zhaomh.command.CommandManager;
import com.zhaomh.command.impl.PermissionCommand;
import com.zhaomh.command.impl.PluginCommand;
import com.zhaomh.config.BotConfig;
import com.zhaomh.core.Accessor;
import com.zhaomh.core.Plugin;
import com.zhaomh.core.ServiceRegistry;
import com.zhaomh.event.EventManager;
import com.zhaomh.logger.Logger;
import com.zhaomh.logger.LoggerFactory;
import com.zhaomh.plugin.PluginManager;
import com.zhaomh.plugin.impl.*;
import com.zhaomh.util.JsonUtil;
import com.zhaomh.web.LoggingWebSocketServer;
import com.zhaomh.web.WebUiServer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 应用程序上下文
 * 实现了 ServiceRegistry（服务注册表）和 Accessor（服务访问器）
 */
public class ApplicationContext implements ServiceRegistry, Accessor {

    private static final Logger log = LoggerFactory.getLogger(ApplicationContext.class);

    // ==================== 核心组件 ====================
    private final BotConfig botConfig;
    private final OneBotClient client;
    private final EventManager eventManager;
    private final WebUiServer webUiServer;
    private final LoggingWebSocketServer logServer;

    // ==================== 对外暴露的访问器（方便调试） ====================
    private final PluginManager pluginManager;
    private final CommandManager commandManager;

    // ==================== 服务注册表（核心数据容器） ====================
    private final Map<Class<?>, Object> registry = new ConcurrentHashMap<>();

    // ==================== 构造函数（启动顺序极其重要） ====================
    public ApplicationContext(String wsUrl) {
        log.info("正在初始化 ApplicationContext...");
        this.logServer = new LoggingWebSocketServer(8078);

        // ---------- 加载配置（无依赖） ----------
        this.botConfig = loadBotConfig();
        register(BotConfig.class, botConfig);
        log.info("配置加载完成");

        // ---------- 创建 WebSocket 客户端（依赖 BotConfig） ----------
        this.client = new OneBotClient(wsUrl);
        register(OneBotClient.class, client);
        log.info("OneBotClient 已创建");

        // ---------- 创建事件管理器（依赖 Accessor） ----------
        this.eventManager = new EventManager(this);
        register(EventManager.class, eventManager);
        log.info("EventManager 已创建");

        // ---------- 创建命令管理器（依赖 EventManager + Accessor + BotConfig） ----------
        this.commandManager = new CommandManager(eventManager, this, botConfig);
        register(CommandManager.class, commandManager);
        log.info("CommandManager 已创建");

        // ---------- 创建插件管理器（依赖 ServiceRegistry） ----------
        this.pluginManager = new PluginManager(this);
        register(PluginManager.class, pluginManager);

        // 准备所有插件类列表（包括核心插件和外部插件）
        List<Class<? extends Plugin>> pluginClasses = List.of(
                BuiltinBotPlugin.class,
                StatusPlugin.class,
                NoRepeatPlugin.class,
                AddOnePlugin.class,
                TestPlugin.class
        );

        // 加载插件（内部自动拓扑排序 + 循环依赖检测 + 状态持久化）
        pluginManager.loadPlugins(pluginClasses);
        pluginManager.loadExternalPlugins();
        log.info("PluginManager 加载完成，已加载 {} 个插件", pluginManager.getAllPlugins().size());


        // ---------- 注册内置系统命令（依赖 PluginManager 和 ServiceRegistry） ----------
        commandManager.register(new PluginCommand(pluginManager));
        commandManager.register(new PermissionCommand(this)); // 权限命令需要访问注册表
        log.info("系统命令注册完成");

        // ---------- 将事件管理器绑定到 WebSocket 客户端 ----------
        client.setEventCallback(eventManager::call);

        // ---------- 创建 WebUi 服务器（依赖 Accessor + PluginManager + OneBotClient + CommandManager） ----------
        this.webUiServer = new WebUiServer(8079, this, pluginManager, client, commandManager);
        log.info("WebUi 服务器已创建");

        log.info("ApplicationContext 初始化完成！");
    }

    // ==================== 生命周期管理 ====================

    /**
     * 启动整个应用（连接 OneBot）
     */
    public void start() {
        log.info("正在启动 OneBot 连接...");
        client.connect();
        log.info("OneBot 连接已建立");
        log.info("启动 WebUi");
        try {
            logServer.start();
            webUiServer.start();
        } catch (IOException e) {
            log.error("Web UI 启动失败", e);
        }
    }

    /**
     * 优雅关闭
     */
    public void shutdown() {
        log.info("正在关闭 ApplicationContext...");
        // 1. 停止 WebUi 服务器
        try {
            webUiServer.stop();
            logServer.stop();
        } catch (InterruptedException e) {
            log.error("日志服务器停止失败", e);
        }
        // 2. 禁用所有插件（触发 onDisable）
       pluginManager.shutdown();

        // 3. 保存配置
        saveBotConfig(botConfig);

        // 4. 断开 WebSocket
        client.close();

        log.info("ApplicationContext 已关闭");
    }

    @Override
    public <T> void register(Class<T> type, T instance) {
        registry.put(type, instance);
        log.debug("注册服务: {} -> {}", type.getSimpleName(), instance.getClass().getSimpleName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> type) {
        T instance = (T) registry.get(type);
        if (instance == null) {
            // 特别提示：如果是核心服务未注册，多半是 BuiltinBotPlugin 未正常加载
            throw new IllegalStateException("服务未注册: " + type.getName() +
                    ". 请确保 BuiltinBotPlugin 已被正确加载并注册了该服务。");
        }
        return instance;
    }

    @Override
    public <T> boolean hasService(Class<T> type) {
        return registry.containsKey(type);
    }

    private BotConfig loadBotConfig() {
        Path configPath = Paths.get("./data/config.json");
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                return JsonUtil.GSON.fromJson(reader, BotConfig.class);
            } catch (Exception e) {
                throw new RuntimeException("读取 BotConfig 失败", e);
            }
        } else {
            BotConfig config = new BotConfig();
            saveBotConfig(config);
            throw new RuntimeException("已生成默认 config.json，请修改 permissions.owner 后重启");
        }
    }

    private void saveBotConfig(BotConfig config) {
        Path configPath = Paths.get("./data/config.json");
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                JsonUtil.GSON.toJson(config, writer);
            }
        } catch (Exception e) {
            throw new RuntimeException("保存配置失败", e);
        }
    }

}