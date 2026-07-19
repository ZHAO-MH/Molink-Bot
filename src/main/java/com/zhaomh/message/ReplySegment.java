package com.zhaomh.message;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zhaomh.id.MessageId;
import com.zhaomh.id.UserId;
import com.zhaomh.util.JsonUtil;
import lombok.Getter;

import java.util.Objects;

public class ReplySegment extends MessageSegment {
    @Getter
    private final MessageId messageId;

    public ReplySegment(MessageId messageId) {
        super("reply", JsonUtil.getSimpleJson("id",messageId.getValue()));
        this.messageId = messageId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ReplySegment that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(messageId, that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(messageId);
    }
}
