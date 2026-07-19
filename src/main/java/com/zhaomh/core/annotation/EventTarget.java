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
package com.zhaomh.core.annotation;

import com.zhaomh.event.Priority;

import java.lang.annotation.*;

/**
 * Marks a method so that the EventManager knows that it should be registered.
 * The priority of the method is also set with this.
 *
 * @author DarkMagician6
 * @see Priority
 * @since July 30, 2013
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventTarget {

    byte value() default Priority.MEDIUM;
}
