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
