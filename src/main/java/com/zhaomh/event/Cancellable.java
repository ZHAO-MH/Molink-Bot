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
 * 简单的接口，应在可取消的事件中实现。
 */
public interface Cancellable {

    /**
     * 获取当前取消状态的活动。
     *
     * @return 如果活动被取消，那是真的。
     */
    boolean isCancelled();

    /**
     * 设定了活动取消的状态。
     *
     * @param state
     *         是否应该取消这个活动。
     */
    void setCancelled(boolean state);

}
