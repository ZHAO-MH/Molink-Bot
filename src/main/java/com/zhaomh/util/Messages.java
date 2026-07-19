package com.zhaomh.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zhaomh.id.MessageId;
import com.zhaomh.logger.Logger;
import com.zhaomh.logger.LoggerFactory;
import com.zhaomh.message.*;
import com.zhaomh.model.User;
import com.zhaomh.id.UserId;
import com.zhaomh.event.impl.BaseEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Messages {
    private static final Logger log = LoggerFactory.getLogger(Messages.class);
    public static AtSegment at(long id) {
        return at(UserId.of(id));
    }

    public static AtSegment at(UserId id) {
        return new AtSegment(id);
    }


    public static AtSegment at(User user) {
        return new AtSegment(user);
    }

    public static TextSegment text(String text) {
        return new TextSegment(text);
    }

    public static TextSegment text(String text, String... args) {
        return new TextSegment(String.format(text, (Object) args));
    }

    public static ImageSegment image(String url) {
        return new ImageSegment(url, null, 0, null, 0);
    }

    public static MessageChain chain(MessageSegment... segments) {
        return MessageChain.of(segments);
    }

    public static MessageChain chain(List<MessageSegment> segments) {
        return new MessageChain(segments);
    }

    public static MessageChain chain(BaseEvent e) {
        JsonObject json = e.getJson();
        if (!"message".equals(json.get("post_type").getAsString()))
            return null;

        JsonArray messages = json.getAsJsonArray("message");
        List<MessageSegment> segments = messages.asList().stream()
                .map(jsonElement -> MessageSegment.fromJson(jsonElement.getAsJsonObject()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return chain(segments);
    }
}
