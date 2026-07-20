/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.config;

import com.google.gson.JsonObject;
import com.zhaomh.util.JsonUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    private final Path path;
    @Getter
    @Setter
    private JsonObject data;

    /**
     * @param file /data/[path]
     */
    public Config(String file) {
        path = Paths.get("./data/" + file);
        try {
            Files.createDirectories(path.getParent());
        } catch (Exception e) {
            throw new RuntimeException("创建配置文件失败", e);
        }
        load();
    }

    private void load() {
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                data = JsonUtil.GSON.fromJson(reader, JsonObject.class);
            } catch (Exception e) {
                throw new RuntimeException("读取配置失败", e);
            }
        } else {
            data = new JsonObject();
            save();
        }
    }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(path)) {
            JsonUtil.GSON.toJson(data, writer);
        } catch (Exception e) {
            throw new RuntimeException("保存配置失败", e);
        }
    }
}
