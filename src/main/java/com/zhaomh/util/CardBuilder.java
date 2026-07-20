/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.util;

public class CardBuilder {

    /**
     * 通用卡片（白底，圆角，阴影）
     */
    public static String build(String title, String content, String footer, String cardStyle) {
        title = escape(title);
        content = escape(content);
        footer = escape(footer);
        return """
            <html>
            <head><meta charset="UTF-8">
              <style>
                html,body{margin:0;padding:0;background:transparent;width:400px;
                  font-family:'Noto Sans SC','Microsoft YaHei',sans-serif;}
                .card{padding:20px;background:#fff;border-radius:10px;
                  box-shadow:0 8px 24px rgba(0,0,0,0.12);color:#333;}
                .title{font-size:20px;font-weight:700;margin-bottom:12px;}
                .content{font-size:15px;color:#555;margin-bottom:16px;white-space:pre-wrap;}
                .footer{font-size:12px;color:#999;border-top:1px solid #eee;padding-top:12px;}
              </style>
            </head>
            <body>
              <div class="card" style="%s">
                <div class="title">%s</div>
                <div class="content">%s</div>
                <div class="footer">%s</div>
              </div>
            </body>
            </html>
            """.formatted(cardStyle, title, content, footer);
    }

    public static String normal(String title, String content, String footer) {
        return build(title, content, footer, "");
    }

    public static String success(String title, String content, String footer) {
        return build(title, content, footer, "background:#eafaf1;border-left:4px solid #2ecc71;");
    }

    public static String warning(String title, String content, String footer) {
        return build(title, content, footer, "background:#fdf2f2;border-left:4px solid #e74c3c;");
    }

    public static String notice(String title, String content, String footer) {
        return build(title, content, footer, "background:#f0f4ff;border-left:4px solid #6a5acd;");
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}