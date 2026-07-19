package com.zhaomh.model;

import com.zhaomh.id.Identifiable;
import com.zhaomh.id.UserId;
import lombok.*;


import com.google.gson.annotations.SerializedName;

@Builder
@AllArgsConstructor
@Getter
@ToString
public class User implements Identifiable<UserId> {
    @SerializedName("user_id")
    private final UserId id;

    @SerializedName("nickname")
    @Builder.Default
    private String nickname = null;

    @SerializedName("sex")
    @Builder.Default
    private String sex = null;

    @SerializedName("age")
    @Builder.Default
    private int age = -1;

    @SerializedName("qid")
    @Builder.Default
    private String qid = null;

    @SerializedName("level")
    @Builder.Default
    private int level = -1;

    @SerializedName("login_days")
    @Builder.Default
    private int loginDays = -1;

    @SerializedName("reg_time")
    @Builder.Default
    private int regTime = -1;

    @SerializedName("long_nick")
    @Builder.Default
    private String longNick = null;

    @SerializedName("city")
    @Builder.Default
    private String city = null;

    @SerializedName("country")
    @Builder.Default
    private String country = null;

    @SerializedName("birthday_year")
    @Builder.Default
    private int birthdayYear = -1;

    @SerializedName("birthday_month")
    @Builder.Default
    private int birthdayMonth = -1;

    @SerializedName("birthday_day")
    @Builder.Default
    private int birthdayDay = -1;

    @SerializedName("labels")
    @Builder.Default
    private String[] labels = null;

    @SerializedName("is_vip")
    @Builder.Default
    private boolean isVip = false;

    @SerializedName("is_years_vip")
    @Builder.Default
    private boolean isYearsVip = false;

    @SerializedName("vip_level")
    @Builder.Default
    private int vipLevel = -1;
}