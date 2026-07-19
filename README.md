# Molink Bot

基于 Onebot v11 实现的机器人框架，实现了QQ的接入。
**推荐使用 [NapCat](https://github.com/NapCatQQ/NapCat) 作为协议端。**

**开发状态：核心架构已稳定，插件系统持续优化中。欢迎测试并提出 Issue 或 PR。**

## 快速开始

### 方案一 - 通过 Docker 运行 （推荐）

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


### 方案二 - 直接运行 Jar

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

## 许可证

本项目核心代码采用 Apache License 2.0 进行许可，详见 LICENSE 文件。
事件系统部分基于 MIT 许可的 EventAPIRemastered，修改后仍遵循 MIT 许可证，原始版权归原作者所有。

基于本框架开发的插件可自由选择许可协议，不受 Apache 2.0 传染性限制。