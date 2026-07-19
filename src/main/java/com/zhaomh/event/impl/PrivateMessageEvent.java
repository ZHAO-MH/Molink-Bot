package com.zhaomh.event.impl;


import com.google.gson.JsonObject;
import com.zhaomh.core.Accessor;

public class PrivateMessageEvent extends MessageEvent {

    public PrivateMessageEvent(JsonObject json, Accessor accessor) {
        super(json, accessor);
    }
}
