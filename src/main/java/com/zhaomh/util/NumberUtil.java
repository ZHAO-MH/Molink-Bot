package com.zhaomh.util;

public class NumberUtil {
    // 生成随机数
    public static int random(int min, int max) {
        return (int) (Math.random() * (max - min + 1) + min);
    }
}
