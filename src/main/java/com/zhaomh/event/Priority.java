/*
 * ============================================================
 * 本文件来源于 EventAPIRemastered 项目，未作功能性修改。
 *
 * 【原始来源】
 *   项目名称  : EventAPIRemastered
 *   原始作者  : lsiem (基于 DarkMagician6 的原始 EventAPI)
 *   原始仓库  : https://github.com/lsiem/EventAPIRemastered
 *   原始路径  : src/com/darkmagician6/eventapi/[类名].java
 *   遵循协议  : MIT License (详见项目根目录 LICENSE.txt)
 *
 * 【变更说明】
 *   仅将包名由 com.darkmagician6.eventapi 调整为 com.zhaomh.event，
 *   代码逻辑、接口定义、注解字段均保持与原版完全一致。
 * ============================================================
 */
package com.zhaomh.event;

/**
 * T调度员优先决定应优先调用哪种方法。
 * Ram说的是我存储数据时的内存占用，所以我决定了
 * 仅用字节作为优先级，因为它们只占用8位内存
 * 每个值，而每个枚举的32位（与整数值相同）。
 *
 */
public final class Priority {

    public static final byte
            /**
             * 最高优先级，先打电话。
             */
            HIGHEST = 0,
    /**
     * 高优先级，优先级最高后被叫来。
     */
    HIGH = 1,
    /**
     * 中等优先级，在高优先级之后被调用。
     */
    MEDIUM = 2,
    /**
     * 低优先级，在中等优先级之后被调用。
     */
    LOW = 3,
    /**
     * 优先级最低，排在所有优先事项之后。
     */
    LOWEST = 4;

    /**
     * 包含所有优先级值的数组。
     */
    public static final byte[] VALUE_ARRAY;

    /**
     * 在该类首次调用时设置VALUE_ARRAY。
     */
    static {
        VALUE_ARRAY = new byte[]{
                HIGHEST,
                HIGH,
                MEDIUM,
                LOW,
                LOWEST
        };
    }

}
