/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.model;

import com.google.gson.annotations.SerializedName;
import com.zhaomh.id.GroupId;
import com.zhaomh.id.Identifiable;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class Group implements Identifiable<GroupId> {
    @SerializedName("group_id")
    private final GroupId id;

    @SerializedName("nickname")
    @Builder.Default
    private String nickname = null;

    public Group(long id, String nickname) {
        this(GroupId.of(id), nickname);
    }
    public Group(GroupId id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }
}
