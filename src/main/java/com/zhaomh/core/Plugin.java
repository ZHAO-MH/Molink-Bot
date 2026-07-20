/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.core;

import com.zhaomh.context.PluginContext;
import lombok.Getter;

public interface Plugin {
    void onLoad(PluginContext context);

    void onUnload();

    void onEnable();

    void onDisable();

    boolean isEnabled();

    String getName();
}
