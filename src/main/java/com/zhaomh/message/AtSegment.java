/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.message;

import com.zhaomh.model.User;
import com.zhaomh.id.UserId;
import com.zhaomh.util.JsonUtil;
import lombok.Getter;

@Getter
public class AtSegment extends MessageSegment {
    private final UserId targetId;

    public AtSegment(User user) {
        this(user.getId());
    }

    public AtSegment(UserId targetId) {
        super("at", JsonUtil.getSimpleJson("qq", targetId.getValue()));
        this.targetId = targetId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AtSegment as) {
            return targetId.equals(as.targetId);
        }
        return false;
    }

    @Override
    public String getPlainText() {
        return "@"+targetId.getValue();
    }
}