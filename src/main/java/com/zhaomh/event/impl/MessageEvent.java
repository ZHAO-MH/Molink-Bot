/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.event.impl;

import com.google.gson.JsonObject;
import com.zhaomh.core.Accessor;
import com.zhaomh.dto.EventSender;
import com.zhaomh.id.MessageId;
import com.zhaomh.logger.Logger;
import com.zhaomh.logger.LoggerFactory;
import com.zhaomh.message.MessageChain;
import com.zhaomh.util.JsonUtil;
import com.zhaomh.util.Messages;
import lombok.Getter;

@Getter
public class MessageEvent extends BaseEvent {
    protected final String rawMessage;
    protected final MessageChain message;
    protected final EventSender sender;
    protected final MessageId messageId;

    protected static final Logger log = LoggerFactory.getLogger(MessageEvent.class);
    public MessageEvent(JsonObject json, Accessor accessor) {
        super(json, accessor);

        this.rawMessage = json.get("raw_message").getAsString();
        this.message = Messages.chain(this);
        this.sender = JsonUtil.fromJson(json.getAsJsonObject("sender"), EventSender.class);
        this.messageId = MessageId.of(json.get("message_seq").getAsInt());
    }
}
