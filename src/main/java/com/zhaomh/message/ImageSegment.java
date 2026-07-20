/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.message;

import com.google.gson.JsonObject;
import lombok.Getter;

@Getter
public class ImageSegment extends MessageSegment{
    private final String url;
    private final String file;
    private final long size;
    private final String summary;
    private final int subType;

    public ImageSegment(String url, String file, long size, String summary, int subType) {
        super("image", new JsonObject());
        data.addProperty("url", url);
        data.addProperty("file", file);
        data.addProperty("file_size", size);
        data.addProperty("summary", summary);
        data.addProperty("sub_type", subType);
        this.url = url;
        this.file = file;
        this.size = size;
        this.summary = summary;
        this.subType = subType;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageSegment that = (ImageSegment) o;
        return url.equals(that.url) && file.equals(that.file);
    }

    @Override
    public String getPlainText() {
        return this.summary != null && !this.summary.isBlank() ? this.summary : "[图片]";
    }
}
