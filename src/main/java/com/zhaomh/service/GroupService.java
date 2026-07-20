/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.service;

import com.zhaomh.id.UserId;
import com.zhaomh.model.Group;
import com.zhaomh.id.GroupId;

public interface GroupService {
    Group getGroup(GroupId groupId);

    void ban(GroupId groupId, UserId userId, int time);
}
