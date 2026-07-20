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
import com.zhaomh.event.EventManager;
import com.zhaomh.core.annotation.EventTarget;
import com.zhaomh.event.impl.GroupMessageEvent;
import com.zhaomh.id.GroupId;
import com.zhaomh.id.UserId;
import com.zhaomh.plugin.BasePlugin;
import com.zhaomh.service.MessageService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AddOnePlugin extends BasePlugin {
    private EventManager eventManager;

    private final Map<GroupId, MessageMeta> messages = new ConcurrentHashMap<>();

    private final int MAX_COUNT = 2;

    @Override
    public void onLoad(PluginContext context) {
        eventManager = context.getService(EventManager.class);
    }

    @Override
    public void onEnable() {
        eventManager.register(this);
    }

    @Override
    public void onDisable() {
        eventManager.unregister(this);
        messages.clear();
    }

    @EventTarget
    public void onGroupMessage(GroupMessageEvent e) {
        GroupId groupId = e.getGroupId();
        if (!messages.containsKey(groupId)) {
            List<UserId> initList = new ArrayList<>();
            initList.add(e.getSender().getUserId());
            messages.put(groupId, new MessageMeta(initList, e.getRawMessage()));
            return;
        }

        MessageMeta meta = messages.get(groupId);
        if (meta.getContent().equals(e.getRawMessage())) {
            if (!meta.getCount().contains(e.getSender().getUserId())) {
                meta.getCount().add(e.getSender().getUserId());
            }
            else return;

            if (meta.getCount().size() >= MAX_COUNT) {
                e.getService(MessageService.class).sendGroupMessage(groupId, e.getMessage());
                messages.remove(groupId);
            }

            return;
        }
        meta.getCount().clear();
        meta.setContent(e.getRawMessage());
        meta.getCount().add(e.getSender().getUserId());
    }

    @Getter
    @AllArgsConstructor
    private static class MessageMeta {
        private List<UserId> count;
        @Setter
        private String content;
    }
}
