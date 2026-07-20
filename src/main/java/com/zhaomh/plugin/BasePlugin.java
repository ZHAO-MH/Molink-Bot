/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.plugin;

import com.zhaomh.context.PluginContext;
import com.zhaomh.core.Plugin;
import com.zhaomh.logger.Logger;
import com.zhaomh.logger.LoggerFactory;
import lombok.Getter;

public abstract class BasePlugin implements Plugin {
    private static final Logger log = LoggerFactory.getLogger(BasePlugin.class);

    private boolean enabled = false;
    @Getter
    private PluginContext context;

    // 由 PluginManager 调用，设置基础信息
    public final void initialize(PluginContext context) {
        this.context = context;
    }

    @Override
    public final boolean isEnabled() {
        return enabled;
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;

        if (enabled) {
            log.info("插件 {} 已启用", context.getPluginName());
            onEnable();
        } else {
            log.info("插件 {} 已禁用", context.getPluginName());
            onDisable();
        }
    }

    public <T> T getService(Class<T> type) {
        return context.getService(type);
    }

    public String getName() {
        return context.getPluginName();
    }

    @Override
    public void onLoad(PluginContext context) {
    }

    @Override
    public void onUnload() {

    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }
}