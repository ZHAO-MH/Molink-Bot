/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
 */

package com.zhaomh.service;

import com.google.gson.JsonObject;
import com.zhaomh.bot.OneBotClient;
import com.zhaomh.dto.ApiResponse;
import com.zhaomh.id.Identifiable;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CachedEntityService<T extends Identifiable<ID>, ID> extends Service {
    protected final Map<ID, T> cache = new ConcurrentHashMap<>();

    public CachedEntityService(OneBotClient client) {
        super(client);
    }

    /**
     * 通用获取或从 API 拉取实体的方法
     * @param id           实体的 ID
     * @param apiMethod    API 名称
     * @param dataType     反序列化目标类型（例如 User.class）
     */
    protected T getOrFetch(ID id, String apiMethod, Type dataType) {
        if (cache.containsKey(id)) {
            return cache.get(id);
        }

        JsonObject params = extractParams(id);

        ApiResponse<T> resp = callApi(apiMethod, params, dataType);
        if (resp != null && resp.isOk()) {
            T entity = resp.getData();
            // 利用 Identifiable 接口获取实体的 ID 并放入缓存
            cache.put(id, entity);
            return entity;
        }
        return null;
    }

    /**
     * 子类只需要告诉基类：如何从 ID 对象中得到 Params 对象
     */
    protected abstract JsonObject extractParams(ID id);
}
