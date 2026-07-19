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
 * 最基本的可阻止事件形式。
 * 可停止事件被单独调用，方法调用被停止
 * 只要EventStoppable被停止。
 *
 * @author DarkMagician6
 * @since 26-9-13
 */
public abstract class EventStoppable implements Event {

    private boolean stopped;

    /**
     * 建造者无需公开。
     */
    protected EventStoppable() {
    }

    /**
     * 将停止状态设置为 true。
     */
    public void stop() {
        stopped = true;
    }

    /**
     * 检查 stopped 布尔值。
     *
     * @return
     *      如果 EventStoppable 已停止，则返回 true。
     */
    public boolean isStopped() {
        return stopped;
    }

}
