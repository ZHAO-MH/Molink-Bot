/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.plugin.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zhaomh.config.Config;
import com.zhaomh.context.CommandContext;
import com.zhaomh.context.PluginContext;
import com.zhaomh.core.annotation.Command;
import com.zhaomh.event.EventManager;
import com.zhaomh.core.annotation.EventTarget;
import com.zhaomh.event.impl.GroupMessageEvent;
import com.zhaomh.id.UserId;
import com.zhaomh.message.AtSegment;
import com.zhaomh.message.MessageChain;
import com.zhaomh.plugin.BasePlugin;
import com.zhaomh.service.GroupMemberService;
import com.zhaomh.service.GroupService;
import com.zhaomh.service.MessageService;
import com.zhaomh.util.Messages;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NoRepeatPlugin extends BasePlugin {
    private final int MAX_COUNT = 3;
    private final int MAX_LENGTH = 100;

    private final Map<UserId, Msg> userMsgs = new ConcurrentHashMap<>();
    private final List<UserId> excludeUsers = new CopyOnWriteArrayList<>();

    private final Config config = new Config("no-repeat.json");

    private EventManager eventManager;

    @Getter
    @Setter
    @AllArgsConstructor
    private static class Msg {
        private String content;
        private int count;
    }

    @Override
    public void onLoad(PluginContext context) {
        eventManager = context.getService(EventManager.class);
    }

    @Override
    public void onEnable() {
        config.getData().getAsJsonArray("exclude_users").forEach(jsonElement -> excludeUsers.add(UserId.of(jsonElement.getAsLong())));
        eventManager.register(this);
    }

    @Override
    public void onDisable() {
        eventManager.unregister(this);
        JsonObject data = new JsonObject();
        JsonArray array = new JsonArray();
        excludeUsers.forEach(userId -> array.add(userId.getValue()));
        data.add("exclude_users", array);
        config.setData(data);
    }

    @EventTarget
    public void onGroupMessage(GroupMessageEvent e) {
        String rw = e.getRawMessage();
        UserId userId = e.getSender().getUserId();

        if (excludeUsers.contains(userId))
            return;

        if (e.getMessage().getPlainText().length() >= MAX_LENGTH) {
            e.getService(MessageService.class).sendGroupMessage(e.getGroupId(), MessageChain.of(Messages.at(e.getSender().getUserId()), Messages.text(" 消息过长，请勿发送过长消息！！！")));
            e.getService(GroupService.class).ban(e.getGroupId(), userId, 5*60);
        }

        Msg msg = userMsgs.getOrDefault(userId, new Msg("", 0));

        if (msg.getContent().equals(rw)) {
            msg.setCount(msg.getCount() + 1);
            if (msg.getCount() > MAX_COUNT) {
                e.getService(MessageService.class).sendGroupMessage(e.getGroupId(), MessageChain.of(Messages.at(userId), Messages.text(" 请勿刷屏！！！")));
                e.getService(GroupService.class).ban(e.getGroupId(), userId, 5*60);
                msg.setCount(0);
                msg.setContent("");
                return;
            }
        } else {
            msg = new Msg(rw, 1);
        }

        userMsgs.put(userId, msg);
    }

    @Command("norepeat/exclude")
    public void excludeUser(CommandContext ctx) {
        AtSegment as = ctx.getMessage().getFirstAtSegment();
        if (as == null || !ctx.isInGroup())
            return;

        excludeUsers.add(as.getTargetId());
        ctx.sendAtLnMessage("已排除关于 " + getService(GroupMemberService.class).getGroupMemberInfo(ctx.getGroupId(), as.getTargetId()).getDisplayName() + " 的重复消息检测");
    }
}
