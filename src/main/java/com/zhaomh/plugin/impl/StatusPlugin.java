/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.plugin.impl;

import com.zhaomh.bot.OneBotClient;
import com.zhaomh.command.CommandManager;
import com.zhaomh.context.CommandContext;
import com.zhaomh.context.PluginContext;
import com.zhaomh.core.annotation.Command;
import com.zhaomh.core.annotation.Core;
import com.zhaomh.plugin.BasePlugin;
import com.zhaomh.plugin.PluginManager;
import com.zhaomh.service.StatusService;
import com.zhaomh.util.StringUtil;

@Core
public class StatusPlugin extends BasePlugin implements StatusService {

    private final long startTime = System.currentTimeMillis();
    private CommandManager commandManager;
    private PluginManager pluginManager ;

    @Override
    public void onLoad(PluginContext context) {
        context.registerService(StatusService.class, this);

        commandManager = getService(CommandManager.class);
        pluginManager = getService(PluginManager.class);
    }

    @Override
    public void onEnable() {
        commandManager.register(this);
    }

    @Override
    public void onDisable() {
        commandManager.unregister(this);
    }

    @Command("status")
    public void status(CommandContext ctx) {
        ctx.sendAtLnMessage(StringUtil.format("""
                状态
                ----------
                运行时长：{}
                内存使用：{}/{} MB
                ----------
                已加载插件：{}
                已注册指令：{}
                """, getUptimeFormatted(), getUsedMemoryMB(), getMaxMemoryMB(), pluginManager.getAllPlugins().size(), commandManager.getCommandCount()));
    }

    // ====== StatusService 实现 ======

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getUptimeMillis() {
        return System.currentTimeMillis() - startTime;
    }

    @Override
    public String getUptimeFormatted() {
        long ms = getUptimeMillis();
        long sec = ms / 1000;
        long min = sec / 60;
        long hour = min / 60;
        long day = hour / 24;
        if (day > 0) return day + "d " + (hour % 24) + "h";
        if (hour > 0) return hour + "h " + (min % 60) + "m";
        if (min > 0) return min + "m " + (sec % 60) + "s";
        return sec + "s";
    }

    @Override
    public long getUsedMemoryMB() {
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
    }

    @Override
    public long getMaxMemoryMB() {
        return Runtime.getRuntime().maxMemory() / (1024 * 1024);
    }

    @Override
    public boolean isBotConnected() {
        return getService(OneBotClient.class).isConnected();
    }
}