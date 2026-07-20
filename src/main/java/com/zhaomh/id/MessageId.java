/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

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
