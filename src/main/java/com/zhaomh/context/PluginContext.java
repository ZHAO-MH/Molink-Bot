/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.context;

import com.zhaomh.core.Accessor;
import com.zhaomh.core.ServiceRegistry;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PluginContext implements Accessor {
    private final ServiceRegistry registry; // 持有注册表，以便插件能注册自己的服务
    private final String pluginName;

    @Override
    public <T> T getService(Class<T> type) {
        return registry.getService(type);
    }

    @Override
    public <T> boolean hasService(Class<T> type) {
        return registry.hasService(type);
    }

    // 提供注册方法（给插件用）
    public <T> void registerService(Class<T> type, T instance) {
        registry.register(type, instance);
    }

}
