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
import com.zhaomh.model.User;
import com.zhaomh.id.UserId;
import com.zhaomh.service.CachedEntityService;
import com.zhaomh.service.UserService;

public class UserServiceImpl extends CachedEntityService<User, UserId> implements UserService {
    public UserServiceImpl(OneBotClient client) {
        super(client);
    }

    @Override
    protected JsonObject extractParams(UserId userId) {
        JsonObject params = new JsonObject();
        params.addProperty("user_id", userId.getValue());
        return params;
    }

    @Override
    public User getUser(UserId id) {
        return getOrFetch(id, "get_stranger_info", User.class);
    }
}
