/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.plugin.impl;

import com.zhaomh.context.PluginContext;
import com.zhaomh.core.annotation.Command;
import com.zhaomh.context.CommandContext;
import com.zhaomh.command.CommandManager;
import com.zhaomh.command.RequirePermission;
import com.zhaomh.message.MessageChain;
import com.zhaomh.model.Role;
import com.zhaomh.plugin.BasePlugin;
import com.zhaomh.util.CardBuilder;
import com.zhaomh.util.HtmlRenderer;
import com.zhaomh.util.NumberUtil;

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

    @Command("test/html/card/normal")
    public void htmlCardNormal(CommandContext ctx) {
        if (ctx.getArgs().length > 0 && NumberUtil.isInteger(ctx.getArgs()[0]))
            ctx.sendMessage(MessageChain.image(HtmlRenderer.render(CardBuilder.normal("Title", "Content", "Footer"),Integer.parseInt(ctx.getArgs()[0]))));
        else ctx.sendMessage(MessageChain.image(HtmlRenderer.render(CardBuilder.normal("Title", "Content", "Footer"),400)));
    }

    @Command("test/html/card/success")
    public void htmlCardSuccess(CommandContext ctx) {
        if (ctx.getArgs().length > 0 && NumberUtil.isInteger(ctx.getArgs()[0]))
            ctx.sendMessage(MessageChain.image(HtmlRenderer.render(CardBuilder.success("Title", "Content", "Footer"),Integer.parseInt(ctx.getArgs()[0]))));
        else ctx.sendMessage(MessageChain.image(HtmlRenderer.render(CardBuilder.success("Title", "Content", "Footer"),400)));
    }

    @Command("test/html/card/warning")
    public void htmlCardWarning(CommandContext ctx) {
        if (ctx.getArgs().length > 0 && NumberUtil.isInteger(ctx.getArgs()[0]))
            ctx.sendMessage(MessageChain.image(HtmlRenderer.render(CardBuilder.warning("Title", "Content", "Footer"),Integer.parseInt(ctx.getArgs()[0]))));
        else ctx.sendMessage(MessageChain.image(HtmlRenderer.render(CardBuilder.warning("Title", "Content", "Footer"),400)));
    }

    @Command("test/html/card/notice")
    public void htmlCardNotice(CommandContext ctx) {
        if (ctx.getArgs().length > 0 && NumberUtil.isInteger(ctx.getArgs()[0]))
            ctx.sendMessage(MessageChain.image(HtmlRenderer.render(CardBuilder.notice("Title", "Content", "Footer"),Integer.parseInt(ctx.getArgs()[0]))));
        else ctx.sendMessage(MessageChain.image(HtmlRenderer.render(CardBuilder.notice("Title", "Content", "Footer"),400)));
    }
}
