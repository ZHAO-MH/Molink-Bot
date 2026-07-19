# 茉莉Bot Molink Bot

基于 Onebot v11 实现的机器人框架，实现了QQ的接入。
推荐使用 [Napcat](https://github.com/napcat/Napcat) 作为协议端使用。

*当前的外部插件读取方法的实现由AI编写+人工粗改，效果不甚良好*

**当前项目正在初步开发阶段，欢迎使用以反馈 Bug，README文件也在逐步完善，下列内容仅供参考。**

## 快速开始

### 方案 1 - 通过 Docker 使用 （推荐）

1. 确保 Docker 已安装
2. 拉取镜像：`docker pull zhaomh/molink-bot`
3. 运行容器：`docker run -d -p 8079:8079 -p 8078:8078 --name molink-bot zhaomh/molink-bot`


### 方案 2 - 直接运行 Jar

1. 从 [Releases](https://github.com/ZHAO-MH/Molink-Bot/releases) 下载最新版本的 Jar 文件。
2. 新建 /data/config.json 文件，输入如下内容：
```json
{
  "bot_id": bot的QQ号,
  "permissions": {
    "owner": 你的QQ号,
    "admins": [
    ]
  }
}
```
3. 确保 8079, 8078 端口没有被占用，通过`java -jar Molink-Bot.jar`启动框架。
4. 向框架对应QQ好友发送`status`，若收到响应，则代表框架正确运行。

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