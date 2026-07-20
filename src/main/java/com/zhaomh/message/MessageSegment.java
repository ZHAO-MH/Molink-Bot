/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.message;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zhaomh.id.MessageId;
import com.zhaomh.id.UserId;
import com.zhaomh.logger.Logger;
import com.zhaomh.logger.LoggerFactory;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MessageSegment {
    private static final Logger log = LoggerFactory.getLogger(MessageSegment.class);
    @Getter
    private final String type;
    protected final JsonObject data;

    public MessageSegment(String type, JsonObject data) {
        this.type = type;
        this.data = data;
    }

    private static final Map<String, Function<JsonObject, MessageSegment>> REGISTRY = new HashMap<>();

    protected static void register(String type, Function<JsonObject, MessageSegment> creator) {
        REGISTRY.put(type, creator);
    }

    static {
        register("at", message -> {
            JsonObject data = message.getAsJsonObject("data");
            JsonElement qq = data.get("qq");
            if (qq == null || "all".equals(qq.getAsString())) return null;
            return new AtSegment(UserId.of(qq.getAsLong()));
        });

        register("image", message -> {
            JsonObject data = message.getAsJsonObject("data");
            String url = data.has("url") ? data.get("url").getAsString() : null;
            String file = data.has("file") ? data.get("file").getAsString() : null;
            long size = data.has("file_size") ? data.get("file_size").getAsLong() : 0L;
            String summary = data.has("summary") ? data.get("summary").getAsString() : null;
            int subType = data.has("sub_type") ? data.get("sub_type").getAsInt() : 0;
            return new ImageSegment(url, file, size, summary, subType);
        });

        register("reply", message -> {
            int id = message.getAsJsonObject("data").get("id").getAsInt();
            return new ReplySegment(MessageId.of(id));
        });

        register("text", message -> {
            String text = message.getAsJsonObject("data").get("text").getAsString();
            return new TextSegment(text);
        });
    }

    public static MessageSegment fromJson(JsonObject message) {
        String type = message.get("type").getAsString();
        Function<JsonObject, MessageSegment> creator = REGISTRY.get(type);
        if (creator == null) {
            return new MessageSegment(type, message.getAsJsonObject("data"));
        }
        return creator.apply(message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageSegment that = (MessageSegment) o;
        return type.equals(that.type) && data.equals(that.data);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+"{" +
                "type='" + type + '\'' +
                ", data=" + data +
                '}';
    }

    public JsonObject getData() {
        return data;
    }
    public String getPlainText() {
        return "";
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.add("data", data);
        return json;
    }
}
