/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.service.impl;

import com.google.gson.JsonObject;
import com.zhaomh.bot.OneBotClient;
import com.zhaomh.id.GroupId;
import com.zhaomh.id.GroupMemberId;
import com.zhaomh.id.UserId;
import com.zhaomh.model.GroupMember;
import com.zhaomh.service.CachedEntityService;
import com.zhaomh.service.GroupMemberService;

public class GroupMemberServiceImpl extends CachedEntityService<GroupMember, GroupMemberId> implements GroupMemberService {
    public GroupMemberServiceImpl(OneBotClient client) {
        super(client);
    }

    @Override
    protected JsonObject extractParams(GroupMemberId groupMemberId) {
        JsonObject params = new JsonObject();
        params.addProperty("group_id", groupMemberId.getGroupId().getValue());
        params.addProperty("user_id", groupMemberId.getUserId().getValue());
        return params;
    }

    @Override
    public GroupMember getGroupMemberInfo(GroupId groupId, UserId userId) {
        return getOrFetch(GroupMemberId.of(groupId, userId), "get_group_member_info", GroupMember.class);
    }
}
