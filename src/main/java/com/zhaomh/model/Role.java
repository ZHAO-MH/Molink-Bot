/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.model;

import lombok.Getter;

@Getter
public enum Role {
    NORMAL(0),
    ADMIN(5),
    OWNER(10);

    private final int level;
    Role(int level) {
        this.level = level;
    }

    public boolean isAtLeast(Role other) {
        return this.level >= other.level;
    }

    public static Role fromLevel(int level) {
        for (Role role : Role.values()) {
            if (role.level == level) {
                return role;
            }
        }
        return NORMAL;
    }
}