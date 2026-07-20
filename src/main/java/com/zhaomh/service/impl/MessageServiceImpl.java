/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.service.impl;

import com.google.gson.JsonObject;
import com.zhaomh.bot.OneBotClient;
import com.zhaomh.id.GroupId;
import com.zhaomh.id.UserId;
import com.zhaomh.message.MessageChain;
import com.zhaomh.service.MessageService;
import com.zhaomh.service.Service;

public class MessageServiceImpl extends Service implements MessageService {
    public MessageServiceImpl(OneBotClient client) {
        super(client);
    }


    @Override
    public void sendGroupMessage(GroupId groupId, MessageChain message) {
        JsonObject params = new JsonObject();
        params.addProperty("group_id", groupId.getValue());
        params.add("message", message.toJsonArray());
        client.callApiAsync("send_group_msg", params);
    }

    @Override
    public void sendPrivateMessage(UserId userId, MessageChain message) {
        JsonObject params = new JsonObject();
        params.addProperty("user_id", userId.getValue());
        params.add("message", message.toJsonArray());
        client.callApiAsync("send_private_msg", params);
    }
}
