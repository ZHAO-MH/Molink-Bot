package com.zhaomh.message;

import com.google.gson.JsonArray;
import com.zhaomh.id.MessageId;
import com.zhaomh.id.UserId;

import java.util.*;

public class MessageChain {
    private final List<MessageSegment> segments;

    public MessageChain(List<MessageSegment> segments) {
        if (segments == null)
            throw new IllegalArgumentException("segments cannot be null");
        this.segments = List.copyOf(segments);
    }

    public MessageChain append(MessageSegment segment) {
        List<MessageSegment> newList = new ArrayList<>(this.segments);
        newList.add(segment);
        return new MessageChain(newList);
    }

    public Set<UserId> getAtTargets() {
        Set<UserId> targets = new HashSet<>();
        for (MessageSegment segment : segments) {
            if (segment instanceof AtSegment atSegment) {
                targets.add(atSegment.getTargetId());
            }
        }
        return targets;
    }

    public AtSegment getFirstAtSegment() {
        for (MessageSegment segment : segments) {
            if (segment instanceof AtSegment atSegment) {
                return atSegment;
            }
        }
        return null;
    }

    public String getPlainText() {
        StringBuilder sb = new StringBuilder();
        for (MessageSegment segment : segments) {
            sb.append(segment.getPlainText());
        }
        return sb.toString();
    }

    public List<MessageSegment> getSegments() {
        // 返回可变列表
        return new ArrayList<>(segments);
    }

    public static MessageChain of(MessageSegment... segments) {
        return new MessageChain(Arrays.asList(segments)) ;
    }

    public static MessageChain fromList(List<MessageSegment> segments) {
        return new MessageChain(segments);
    }

    public static MessageChain text(String text) {
        return MessageChain.of(new TextSegment(text));
    }

    public static MessageChain reply(MessageId messageId, MessageSegment... segments) {
        ArrayList<MessageSegment> list = new ArrayList<>();
        list.add(new ReplySegment(messageId));
        list.addAll(Arrays.asList(segments));
        return MessageChain.fromList(list);
    }

    public static MessageChain reply(MessageId messageId, MessageChain messageChain) {
        ArrayList<MessageSegment> list = new ArrayList<>();
        list.add(new ReplySegment(messageId));
        list.addAll(messageChain.getSegments());
        return MessageChain.fromList(list);
    }

    public JsonArray toJsonArray() {
        JsonArray array = new JsonArray();
        for (MessageSegment segment : segments) {
            array.add(segment.toJson());
        }
        return array;
    }

    public int size() {
        return segments.size();
    }

    public boolean isEmpty() {
        return segments.isEmpty();
    }

    public MessageSegment get(int index) {
        return segments.get(index);
    }

    public MessageChain subChain(int fromIndex, int toIndex) {
        return new MessageChain(segments.subList(fromIndex, toIndex));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MessageChain chain) {
            if (o == this)
                return true;
            return segments.equals(chain.segments);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(segments);
    }
}
