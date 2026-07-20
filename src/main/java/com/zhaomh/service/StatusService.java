/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.service;

public interface StatusService {
    long getStartTime();

    long getUptimeMillis();

    String getUptimeFormatted();

    long getUsedMemoryMB();

    long getMaxMemoryMB();

    boolean isBotConnected();
}