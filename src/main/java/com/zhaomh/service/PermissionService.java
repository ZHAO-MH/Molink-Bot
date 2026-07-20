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
import com.zhaomh.model.Role;

public interface PermissionService {
    boolean hasPermission(UserId userId, Role requiredRole);
    void setPermission(UserId userId, Role requiredRole);
    Role getRole(UserId userId);

    boolean isOwner(UserId userId);
    boolean isAdmin(UserId userId);
    boolean isNormal(UserId userId);
}
