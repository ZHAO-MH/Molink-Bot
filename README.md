# Molink Bot

基于 Onebot v11 实现的机器人框架，实现了QQ的接入。
**推荐使用 [NapCat](https://github.com/NapCatQQ/NapCat) 作为协议端。**

**开发状态：核心架构已稳定，插件系统持续优化中。欢迎测试并提出 Issue 或 PR。**

## 快速开始

### 方案 1 - 通过 Docker 使用 （推荐）

1. 确保 Docker 已安装
3. 拉取并运行容器：
```
docker run -d \
  -p 8079:8079 \  # Web 管理面板
  -p 8078:8078 \  # WebSocket 服务端口
  -e HOST=0.0.0.0 \
  -e NAP_PORT=你的napcat端口 \
  -e WS_TOKEN=你的ws_token \
  -v ./data:/app/data \
  -v ./plugins:/app/plugins \
  --name molink-bot zhaomh/molink-bot:latest
```


### 方案 2 - 直接运行 Jar

1. 从 [Releases](https://github.com/ZHAO-MH/Molink-Bot/releases) 下载最新版本的 Jar 文件。
2. 新建 ./data/config.json 文件，输入如下内容：
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

本项目框架核心采用 GNU Lesser General Public License v3.0。
使用本框架编写的插件不被视为衍生作品，可以按独立许可发布。