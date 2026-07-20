/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.config;

import com.google.gson.annotations.SerializedName;
import com.zhaomh.id.UserId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@ToString
public class BotConfig {
    @SerializedName("bot_id")
    @Getter
    private UserId botId = UserId.of(-1);

    @SerializedName("permissions")
    @Getter
    private Permissions permissions = new Permissions();

    @ToString
    @Getter
    @Setter
    public static class Permissions {
        @SerializedName("owner")
        private UserId owner = UserId.of(-1);
        @SerializedName("admins")
        private List<UserId> admins = new ArrayList<>();

        public List<UserId> getAdmins() {
            return new ArrayList<>(admins);
        }

        public boolean addAdmin(UserId userId) {
            if (!admins.contains(userId)) {
                admins.add(userId);
                return true;
            }
            return false;
        }

        public boolean removeAdmin(UserId userId) {
            return admins.remove(userId);
        }

        protected void setAdmins(List<UserId> admins) {
            this.admins = admins != null ? new ArrayList<>(admins) : new ArrayList<>();
        }
    }

    public BotConfig() {
    }


    public UserId getOwner() {
        return permissions.getOwner();
    }

    public void setOwner(UserId owner) {
        permissions.setOwner(owner);
    }

    public List<UserId> getAdmins() {
        return permissions.getAdmins();
    }

    public void addAdmin(UserId userId) {
        permissions.addAdmin(userId);
    }

    public void removeAdmin(UserId userId) {
        permissions.removeAdmin(userId);
    }
}