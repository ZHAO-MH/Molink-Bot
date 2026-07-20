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
import com.zhaomh.id.GroupMemberId;
import com.zhaomh.id.Identifiable;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class GroupMember implements Identifiable<GroupMemberId> {
    private GroupMemberId id;

    @SerializedName("nickname")
    @Builder.Default
    private String nickname = null;

    @SerializedName("card")
    @Builder.Default
    private String card = null;

    @SerializedName("sex")
    @Builder.Default
    private String sex = null;

    @SerializedName("age")
    @Builder.Default
    private int age = -1;

    @SerializedName("join_time")
    @Builder.Default
    private int joinTime = -1;

    @SerializedName("last_sent_time")
    @Builder.Default
    private int lastSentTime = -1;

    @SerializedName("level")
    @Builder.Default
    private int level = -1;

    @SerializedName("qq_level")
    @Builder.Default
    private int qqLevel = -1;

    @SerializedName("role")
    @Builder.Default
    private String groupRole = null;

    @SerializedName("title")
    @Builder.Default
    private String title = null;

    @SerializedName("area")
    @Builder.Default
    private String area = null;

    @SerializedName("unfriendly")
    @Builder.Default
    private boolean unfriendly = false;

    @SerializedName("title_expire_time")
    @Builder.Default
    private int titleExpireTime = -1;

    @SerializedName("card_changeable")
    @Builder.Default
    private boolean cardChangeable = false;

    @SerializedName("shut_up_timestamp")
    @Builder.Default
    private int shutUpTimestamp = -1;

    @SerializedName("is_robot")
    @Builder.Default
    private boolean isRobot = false;

    @SerializedName("qage")
    @Builder.Default
    private int qage = -1;

    public String getDisplayName() {
        return this.card != null && !this.card.isBlank() ? this.card : this.nickname;
    }
}