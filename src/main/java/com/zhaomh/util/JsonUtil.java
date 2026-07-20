/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class JsonUtil {
    public static final Gson GSON = new GsonBuilder().serializeNulls().create();

    public static <T> T fromJson(JsonObject data, Class<T> clazz) {
        return GSON.fromJson(data, clazz);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    public static JsonObject getSimpleJson(String key, long value) {
        JsonObject json = new JsonObject();
        json.addProperty(key, value);
        return json;
    }

    public static JsonObject getSimpleJson(String key, String value) {
        JsonObject json = new JsonObject();
        json.addProperty(key, value);
        return json;
    }
}