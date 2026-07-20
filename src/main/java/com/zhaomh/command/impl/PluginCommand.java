/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.command.impl;

import com.zhaomh.builtin.BuiltinBotPlugin;
import com.zhaomh.core.annotation.Command;
import com.zhaomh.context.CommandContext;
import com.zhaomh.core.Plugin;
import com.zhaomh.plugin.PluginManager;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PluginCommand {
    private final PluginManager pluginManager;

    public PluginCommand(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    // ---------- 原有命令保持不变 ----------
    @Command("plugin/list")
    public void listPlugins(CommandContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("插件列表：[");
        for (Plugin plugin : pluginManager.getAllPlugins()) {
            sb.append(plugin.getName())
                    .append(plugin.isEnabled() ? "√" : "×")
                    .append(", ");
        }
        if (sb.length() > 5) { // 删除最后的 ", "
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("]");
        ctx.sendMessage(sb.toString());
    }

    @Command("plugin/enable")
    public void enablePlugin(CommandContext ctx) {
        Plugin plugin = check(ctx);
        if (plugin == null) return;
        if (plugin.isEnabled()) {
            ctx.sendAtMessage(ctx.getArgs()[0] + " 插件已启用，无法再次启用");
            return;
        }
        pluginManager.enablePlugin(plugin.getName());
        ctx.sendAtMessage(ctx.getArgs()[0] + " 插件启用成功");
    }

    @Command("plugin/disable")
    public void disablePlugin(CommandContext ctx) {
        Plugin plugin = check(ctx);
        if (plugin == null) return;
        if (!plugin.isEnabled()) {
            ctx.sendAtMessage(ctx.getArgs()[0] + " 插件已关闭，无法再次关闭");
            return;
        }
        if (plugin instanceof BuiltinBotPlugin) {
            ctx.sendAtMessage("内置核心插件无法关闭！");
            return;
        }
        pluginManager.disablePlugin(plugin.getName());
        ctx.sendAtMessage(ctx.getArgs()[0] + " 插件关闭成功");
    }

    @Command("plugin/load")
    public void loadPlugin(CommandContext ctx) {
        if (ctx.getArgs().length == 0) {
            ctx.sendAtMessage("请输入要加载的插件名称");
            return;
        }
        String name = ctx.getArgs()[0];

        if (pluginManager.getPluginByName(name) != null) {
            ctx.sendAtMessage(name + " 插件已加载，如需更新请使用 reload");
            return;
        }

        // 从插件目录加载
        pluginManager.loadPluginByName(name);
        // 重新检查是否加载成功
        if (pluginManager.getPluginByName(name) != null) {
            ctx.sendAtMessage(name + " 插件加载成功");
        } else {
            ctx.sendAtMessage(name + " 插件加载失败，请检查文件是否存在");
        }
    }

    @Command("plugin/unload")
    public void unloadPlugin(CommandContext ctx) {
        if (ctx.getArgs().length == 0) {
            ctx.sendAtMessage("请输入要卸载的插件名称");
            return;
        }
        String name = ctx.getArgs()[0];

        Plugin plugin = pluginManager.getPluginByName(name);
        if (plugin instanceof BuiltinBotPlugin) {
            ctx.sendAtMessage("内置核心插件无法卸载！");
            return;
        }

        if (plugin == null) {
            ctx.sendAtMessage(name + " 插件未加载，无需卸载");
            return;
        }

        try {
            pluginManager.unloadPluginByName(name);
            ctx.sendAtMessage(name + " 插件已卸载");
        } catch (Exception e) {
            ctx.sendAtMessage(name + " 插件卸载失败：" + e.getMessage());
        }
    }

    @Command("plugin/reload")
    public void reloadPlugin(CommandContext ctx) {
        if (ctx.getArgs().length == 0) {
            ctx.sendAtMessage("请输入要重载的插件名称");
            return;
        }
        String name = ctx.getArgs()[0];

        Plugin plugin = pluginManager.getPluginByName(name);
        if (plugin instanceof BuiltinBotPlugin) {
            ctx.sendAtMessage("内置核心插件无法重载！");
            return;
        }

        if (plugin == null) {
            ctx.sendAtMessage(name + " 插件未加载，正在直接加载...");
            pluginManager.loadPluginByName(name);
            if (pluginManager.getPluginByName(name) != null) {
                ctx.sendAtMessage(name + " 加载成功");
            } else {
                ctx.sendAtMessage(name + " 加载失败");
            }
            return;
        }

        try {
            pluginManager.reloadPluginByName(name);
            ctx.sendAtMessage(name + " 插件重载成功");
        } catch (Exception e) {
            ctx.sendAtMessage(name + " 插件重载失败：" + e.getMessage());
        }
    }

    private Plugin check(CommandContext ctx) {
        if (ctx.getArgs().length == 0) {
            ctx.sendAtMessage("请输入插件名称");
            return null;
        }
        Plugin plugin = pluginManager.getPluginByName(ctx.getArgs()[0]);
        if (plugin == null) {
            ctx.sendAtMessage(ctx.getArgs()[0] + " 插件不存在");
            return null;
        }
        return plugin;
    }
}