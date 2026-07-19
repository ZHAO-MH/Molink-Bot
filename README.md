# 茉莉Bot Molink Bot

基于 Onebot v11 实现的机器人框架，实现了QQ的接入。
推荐使用 [Napcat](https://github.com/napcat/Napcat) 作为协议端使用。

*当前的外部插件读取方法的实现由AI编写+人工粗改，效果不甚良好*

## 快速开始

## 构建

## 第三方引用

本项目中的事件系统基于 **[EventAPIRemastered](https://github.com/lsiem/EventAPIRemastered)** 重构。

- **核心引用类**：`EventManager.java`（事件注册与分发引擎）
- **原始作者**：DarkMagician6 / lsiem
- **引用协议**：MIT License
- **我们的修改**：
    - `EventManager` 由静态类改为实例类，注入业务服务（Services）。
    - 新增 `call(JsonObject)` 方法以支持 Go-CQHTTP 协议。
    - 内部 `MethodData` 改用 Java Record 实现。
    - 包名由 `com.darkmagician6.eventapi` 改为 `com.zhaomh.event`。

> 修改部分同样遵循 MIT 协议，原始版权归 lsiem 所有。