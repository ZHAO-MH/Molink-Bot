/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.message;

import com.zhaomh.util.JsonUtil;
import lombok.Getter;

@Getter
public class TextSegment extends MessageSegment{
    private final String text;
    public TextSegment(String text) {
        super("text", JsonUtil.getSimpleJson("text", text));
        this.text = text;
    }

    @Override
    public String getPlainText() {
        return text;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TextSegment ts) {
            return text.equals(ts.text);
        }
        return false;
    }
}
