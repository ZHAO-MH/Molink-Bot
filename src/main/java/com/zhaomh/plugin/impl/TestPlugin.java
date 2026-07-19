package com.zhaomh.plugin.impl;

import com.zhaomh.context.PluginContext;
import com.zhaomh.core.annotation.Command;
import com.zhaomh.context.CommandContext;
import com.zhaomh.command.CommandManager;
import com.zhaomh.command.RequirePermission;
import com.zhaomh.model.Role;
import com.zhaomh.plugin.BasePlugin;

public class TestPlugin extends BasePlugin {
    private CommandManager commandManager;

    @Override
    public void onLoad(PluginContext context) {
        commandManager = context.getService(CommandManager.class);
    }

    @Override
    public void onEnable() {
        commandManager.register(this);
    }

    @Override
    public void onDisable() {
        commandManager.unregister(this);
    }

    @Command("test")
    public void test(CommandContext ctx) {
        ctx.sendAtLnMessage("Test command.");
    }

    @Command("test/repeat")
    public void repeat(CommandContext ctx) {
        ctx.sendAtLnMessage(ctx.getMessage());
    }

    @Command("test/info")
    public void info(CommandContext ctx) {
        ctx.sendAtLnMessage("Your information: " + ctx.getSender().toString());
    }

    @Command("test/admin")
    @RequirePermission(Role.ADMIN)
    public void help(CommandContext ctx) {
        ctx.sendAtLnMessage("You have admin permission.");
    }

    @Command("test/reply")
    public void reply(CommandContext ctx) {
        ctx.sentReplyMessage("This is a reply message.");
    }
}
