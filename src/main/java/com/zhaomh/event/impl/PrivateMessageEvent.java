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

public class PrivateMessageEvent extends MessageEvent {

    public PrivateMessageEvent(JsonObject json, Accessor accessor) {
        super(json, accessor);
    }
}
