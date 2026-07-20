/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

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
