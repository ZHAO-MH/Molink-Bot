/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.serialization;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.zhaomh.id.GroupId;

import java.io.IOException;

public class GroupIdTypeAdapter extends TypeAdapter<GroupId> {
    @Override
    public void write(JsonWriter out, GroupId value) throws IOException {
        out.value(value == null ? null : value.getValue());
    }

    @Override
    public GroupId read(JsonReader in) throws IOException {
        return GroupId.of(in.nextLong());
    }
}
