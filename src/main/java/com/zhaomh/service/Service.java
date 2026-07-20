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
import com.google.gson.reflect.TypeToken;
import com.zhaomh.bot.OneBotClient;
import com.zhaomh.dto.ApiResponse;
import com.zhaomh.util.JsonUtil;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

public class Service {
    protected final OneBotClient client;

    public Service(OneBotClient client) {
        this.client = client;
    }

    /**
     * 通用 API 调用：返回 ApiResponse<T>，其中 data 字段为指定类型
     * @param apiMethod   API 名称，如 "get_stranger_info"
     * @param params      请求参数
     * @param dataType    data 字段的目标类型
     */
    protected <T> ApiResponse<T> callApi(String apiMethod, JsonObject params, Type dataType) {
        try {
            JsonObject response = client.callApiAsync(apiMethod, params)
                    .get(5, TimeUnit.SECONDS);
            // 解析整个 response 为 ApiResponse
            Type apiRespType = TypeToken.getParameterized(ApiResponse.class, dataType).getType();
            return JsonUtil.GSON.fromJson(response, apiRespType);
        } catch (Exception e) {
            // 日志记录
            return null;
        }
    }
}
