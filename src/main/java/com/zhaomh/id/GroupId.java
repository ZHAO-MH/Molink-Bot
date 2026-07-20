/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.id;

import com.google.gson.annotations.JsonAdapter;
import com.zhaomh.serialization.GroupIdTypeAdapter;
import lombok.Getter;

@Getter
@JsonAdapter(GroupIdTypeAdapter.class)
public class GroupId {
    private final long value;

    private GroupId(long value) {
        this.value = value;
    }

    public static GroupId of(long value) {
        return new GroupId(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupId)) return false;
        return value == ((GroupId) o).value;
    }
    @Override
    public int hashCode() { return Long.hashCode(value); }
    @Override
    public String toString() { return String.valueOf(value); }
}
