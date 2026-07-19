package com.zhaomh.message;


import com.zhaomh.util.JsonUtil;
import lombok.Getter;


@Getter
public class TextSegment extends MessageSegment{
    private final String text;
    public TextSegment(String text) {
        super("text", JsonUtil.getSimpleJson("text", text));
        this.text = text;
    }

    @Override
    public String getPlainText() {
        return text;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TextSegment ts) {
            return text.equals(ts.text);
        }
        return false;
    }
}
