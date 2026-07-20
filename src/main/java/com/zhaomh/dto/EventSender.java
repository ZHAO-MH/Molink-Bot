/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.dto;

import com.google.gson.annotations.SerializedName;
import com.zhaomh.id.UserId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Builder
@ToString
public class EventSender {
    @SerializedName("user_id")
    private final UserId userId;

    @SerializedName("nickname")
    @Builder.Default
    private String nickname = null;

    @SerializedName("card")
    @Builder.Default
    private String card = null;      // 私聊时为 null

    @SerializedName("role")
    @Builder.Default
    private String groupRole = null;      // 私聊时为 null

    @SerializedName("title")
    @Builder.Default
    private String title = null;     // 私聊时为 null

    @SerializedName("level")
    @Builder.Default
    private int level = -1;  // 私聊时为 -1

    public boolean  isInGroup() {
        return level != -1;
    }

    public String getDisplayName() {
        return this.card != null && !this.card.isBlank() ? this.card : this.nickname;
    }

    public boolean isGroupAdmin() {
        return "admin".equals(groupRole) || "owner".equals(groupRole);
    }
}
