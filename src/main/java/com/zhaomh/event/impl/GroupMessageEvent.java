package com.zhaomh.event.impl;


import com.google.gson.JsonObject;
import com.zhaomh.core.Accessor;
import com.zhaomh.id.GroupId;
import lombok.Getter;

@Getter
public class GroupMessageEvent extends MessageEvent {
    private final GroupId groupId;

    public GroupMessageEvent(JsonObject json, Accessor accessor) {
        super(json, accessor);
        this.groupId = GroupId.of(json.get("group_id").getAsLong());
    }

}
