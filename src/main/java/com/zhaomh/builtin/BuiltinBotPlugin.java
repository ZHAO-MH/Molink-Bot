/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.builtin;

import com.zhaomh.bot.OneBotClient;
import com.zhaomh.config.BotConfig;
import com.zhaomh.context.PluginContext;
import com.zhaomh.core.annotation.Core;
import com.zhaomh.plugin.BasePlugin;
import com.zhaomh.service.*;
import com.zhaomh.service.impl.*;

@Core
public class BuiltinBotPlugin extends BasePlugin {
    public BuiltinBotPlugin() {
    }

    @Override
    public void onLoad(PluginContext context) {
        OneBotClient client = context.getService(OneBotClient.class);
        context.getRegistry().register(MessageService.class, new MessageServiceImpl(client));
        context.getRegistry().register(PermissionService.class, new PermissionServiceImpl(context.getService(BotConfig.class)));
        context.getRegistry().register(GroupService.class, new GroupServiceImpl(client));
        context.getRegistry().register(UserService.class, new UserServiceImpl(client));
        context.getRegistry().register(GroupMemberService.class, new GroupMemberServiceImpl(client));
        System.out.println("[BuiltinBotPlugin] 核心服务注册完成");
    }
}