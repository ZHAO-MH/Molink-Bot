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
import com.zhaomh.id.UserId;
import com.zhaomh.model.Group;
import com.zhaomh.id.GroupId;
import com.zhaomh.service.CachedEntityService;
import com.zhaomh.service.GroupService;

public class GroupServiceImpl extends CachedEntityService<Group, GroupId> implements GroupService {

    public GroupServiceImpl(OneBotClient client) {
        super(client);
    }

    @Override
    protected JsonObject extractParams(GroupId groupId) {
        JsonObject params = new JsonObject();
        params.addProperty("group_id", groupId.getValue());
        return params;
    }

    @Override
    public Group getGroup(GroupId id) {
        return getOrFetch(id, "get_group_info",  Group.class);
    }

    @Override
    public void ban(GroupId groupId, UserId userId, int time) {
        JsonObject params = new JsonObject();
        params.addProperty("group_id", groupId.getValue());
        params.addProperty("user_id", userId.getValue());
        params.addProperty("duration", time);
        client.callApiAsync("set_group_ban", params);
    }
}
