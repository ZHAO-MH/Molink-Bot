/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.message;

import com.zhaomh.id.MessageId;
import com.zhaomh.util.JsonUtil;
import lombok.Getter;

import java.util.Objects;

public class ReplySegment extends MessageSegment {
    @Getter
    private final MessageId messageId;

    public ReplySegment(MessageId messageId) {
        super("reply", JsonUtil.getSimpleJson("id",messageId.getValue()));
        this.messageId = messageId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ReplySegment that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(messageId, that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(messageId);
    }
}
