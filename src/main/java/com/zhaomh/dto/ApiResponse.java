package com.zhaomh.dto;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    @SerializedName("data")
    private final T data;

    @SerializedName("status")
    private final String status;

    @SerializedName("retcode")
    private final int retcode;

    public boolean isOk() {
        return "ok".equals(status) && retcode == 0;
    }
}
