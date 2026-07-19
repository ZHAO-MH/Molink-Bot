package com.zhaomh.message;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zhaomh.model.User;
import com.zhaomh.id.UserId;
import com.zhaomh.util.JsonUtil;
import lombok.Getter;

@Getter
public class AtSegment extends MessageSegment {
    private final UserId targetId;

    public AtSegment(User user) {
        this(user.getId());
    }

    public AtSegment(UserId targetId) {
        super("at", JsonUtil.getSimpleJson("qq", targetId.getValue()));
        this.targetId = targetId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AtSegment as) {
            return targetId.equals(as.targetId);
        }
        return false;
    }

    @Override
    public String getPlainText() {
        return "@"+targetId.getValue();
    }
}