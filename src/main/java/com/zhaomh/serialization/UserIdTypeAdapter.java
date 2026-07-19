package com.zhaomh.serialization;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.zhaomh.id.UserId;

import java.io.IOException;

public class UserIdTypeAdapter extends TypeAdapter<UserId> {
    @Override
    public void write(JsonWriter out, UserId value) throws IOException {
        out.value(value == null ? null : value.getValue());
    }

    @Override
    public UserId read(JsonReader in) throws IOException {
        return UserId.of(in.nextLong());
    }
}
