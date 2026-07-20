/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.util;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.ScreenshotType;
import com.microsoft.playwright.options.WaitUntilState;

/**
 * HTML 渲染器，全局复用浏览器实例。
 * 应在应用启动时调用 init()，关闭时调用 shutdown()。
 */
public class HtmlRenderer {

    private static Browser browser;
    private static Playwright playwright;

    public static void init() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(true)
                .setArgs(java.util.List.of(
                        "--no-sandbox",
                        "--disable-setuid-sandbox",
                        "--disable-dev-shm-usage",
                        "--disable-gpu",
                        "--disable-software-rasterizer",
                        "--disable-extensions"
                ))
        );
    }

    /**
     * 将 HTML 渲染为 PNG 字节数组（背景透明）
     * @param html  完整 HTML 字符串
     * @param width 视口宽度，应与 body 宽度一致
     */
//    public static byte[] render(String html, int width) {
//        try (Page page = browser.newPage()) {
//            page.setViewportSize(width, 0);
//            page.setContent(html, new Page.SetContentOptions()
//                    .setWaitUntil(WaitUntilState.NETWORKIDLE)
//                    .setTimeout(10_000));
//            page.waitForTimeout(300);
//            return page.screenshot(new Page.ScreenshotOptions()
//                    .setFullPage(true)
//                    .setOmitBackground(true)   // 透明背景
//                    .setType(ScreenshotType.PNG)
//                    .setTimeout(10_000));
//        }
//    }
    public static byte[] render(String html, int width) {
        try (Page page = browser.newPage()) {
            page.setViewportSize(width, 720);      // 给一个初始高度，避免 0 计算
            page.setContent(html, new Page.SetContentOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                    .setTimeout(10_000));

            // 等待字体渲染
            page.waitForTimeout(500);

            // 定位到 .card 元素
            ElementHandle card = page.querySelector(".card");
            if (card == null) {
                // 降级到全页截图
                return page.screenshot(new Page.ScreenshotOptions()
                        .setOmitBackground(true)
                        .setType(ScreenshotType.PNG)
                        .setTimeout(15_000));
            }

            // 直接截取 .card 元素，背景透明
            byte[] result = card.screenshot(new ElementHandle.ScreenshotOptions()
                    .setOmitBackground(true)
                    .setType(ScreenshotType.PNG)
                    .setTimeout(10_000));
            card.dispose();
            return result;
        }
    }

    public static void shutdown() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}