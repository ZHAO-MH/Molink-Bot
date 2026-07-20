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
import com.zhaomh.id.GroupId;
import lombok.Getter;

@Getter
public class GroupMessageEvent extends MessageEvent {
    private final GroupId groupId;

    public GroupMessageEvent(JsonObject json, Accessor accessor) {
        super(json, accessor);
        this.groupId = GroupId.of(json.get("group_id").getAsLong());
    }

}
