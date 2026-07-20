/*
 * ============================================================
 * 【原始来源】
 *   项目名称  : EventAPIRemastered
 *   原始作者  : lsiem (基于 DarkMagician6 的原始 EventAPI)
 *   原始仓库  : https://github.com/lsiem/EventAPIRemastered
 *   原始路径  : src/com/darkmagician6/eventapi/EventTarget.java
 *   遵循协议  : MIT License (详见项目根目录 LICENSE.txt)
 *
 * 【修改/再分发声明】
 *   本文件已被修改并整合至 [你的项目名] 中。
 *   根据 MIT 许可证的要求，以上原始版权声明和免责声明已保留。
 *   本修改后的版本整体依据 Mozilla Public License 2.0 (MPL-2.0) 分发。
 *   您可以在遵守 MPL-2.0 条款的前提下使用本文件。
 * ==============================================================
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 * Copyright (C) 2026 ZHAO-MH
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
