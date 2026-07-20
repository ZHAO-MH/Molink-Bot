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
import com.zhaomh.serialization.UserIdTypeAdapter;
import lombok.Getter;

@Getter
@JsonAdapter(UserIdTypeAdapter.class)
public class UserId {
    private final long value;

    private UserId(long value) {
        this.value = value;
    }

    public static UserId of(long value) {
        return new UserId(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserId)) return false;
        return value == ((UserId) o).value;
    }
    @Override
    public int hashCode() { return Long.hashCode(value); }
    @Override
    public String toString() { return String.valueOf(value); }
}
