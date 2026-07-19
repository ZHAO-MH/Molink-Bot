package com.zhaomh.id;

import lombok.Getter;

@Getter
public class MessageId {
    private final int value;
    private MessageId(int value) {
        this.value = value;
    }

    public static MessageId of(int value) {
        return new MessageId(value);
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MessageId && ((MessageId) obj).value == value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
