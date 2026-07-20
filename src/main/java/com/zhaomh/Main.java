/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context = new ApplicationContext(getWsuRrl());

        Runtime.getRuntime().addShutdownHook(new Thread(context::shutdown));

        context.start();
    }

    private static String getWsuRrl() {
        return "ws://" + System.getenv().getOrDefault("HOST", "0.0.0.0")
                + ":"
                + System.getenv().getOrDefault("PORT", "3001")
                + "?access_token=" + System.getenv().getOrDefault("TOKEN", "java_zhaomh");
    }
}