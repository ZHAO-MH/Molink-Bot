package com.zhaomh.event.impl;

import com.google.gson.JsonObject;
import com.zhaomh.core.Accessor;
import com.zhaomh.event.Event;
import lombok.Getter;

public class BaseEvent implements Event {
    @Getter
    protected JsonObject json;
    @Getter
    private final long time;
    protected final Accessor accessor;

    public BaseEvent(JsonObject json, Accessor  accessor) {
        this.json = json;
        this.time = json.get("time").getAsLong();
        this.accessor = accessor;
    }

    public <T> T getService(Class<T> type) {
        return accessor.getService(type);
    }
}
