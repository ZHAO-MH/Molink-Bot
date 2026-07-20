/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.command.impl;

import com.zhaomh.core.Accessor;
import com.zhaomh.core.annotation.Command;
import com.zhaomh.context.CommandContext;
import com.zhaomh.command.RequirePermission;
import com.zhaomh.message.AtSegment;
import com.zhaomh.model.Role;
import com.zhaomh.service.GroupMemberService;
import com.zhaomh.service.PermissionService;

import java.util.Locale;

public class PermissionCommand {
    private final Accessor accessor;
    public PermissionCommand(Accessor accessor) {
        this.accessor = accessor;
    }

    @Command("perm/get")
    public void getPermission(CommandContext ctx) {
        AtSegment as = ctx.getMessage().getFirstAtSegment();
        if (as == null) {
            ctx.sendAtMessage("你当前的权限等级是：" + accessor.getService(PermissionService.class).getRole(ctx.getSender().getUserId()));
        } else {
            ctx.sendAtMessage(accessor.getService(GroupMemberService.class).getGroupMemberInfo(ctx.getGroupId(), as.getTargetId()).getDisplayName()+" 当前的权限等级是：" + accessor.getService(PermissionService.class).getRole(as.getTargetId()));
        }
    }

    @Command("perm/set")
    @RequirePermission(Role.ADMIN)
    public void setPermission(CommandContext ctx) {
        AtSegment as = ctx.getMessage().getFirstAtSegment();
        if (as == null || ctx.getArgs().length < 2) {
            return;
        }

        switch (ctx.getArgs()[1].toLowerCase(Locale.ROOT)) {
            case "normal" -> accessor.getService(PermissionService.class).setPermission(as.getTargetId(), Role.NORMAL);
            case "admin" -> accessor.getService(PermissionService.class).setPermission(as.getTargetId(), Role.ADMIN);
            case "owner" -> {
                ctx.sendAtMessage("Owner 权限禁止外部修改，仅支持从配置文件修改！");
                return;
            }
            default -> {
                ctx.sendAtMessage("无效的权限等级！仅支持 normal, admin, owner");
                return;
            }
        }

        ctx.sendAtMessage("已设置 " + accessor.getService(GroupMemberService.class).getGroupMemberInfo(ctx.getGroupId(), as.getTargetId()).getDisplayName() + " 的权限等级为 " + ctx.getArgs()[1]);
    }
}
