package com.zhaomh.plugin.impl;

import com.zhaomh.context.PluginContext;
import com.zhaomh.event.EventManager;
import com.zhaomh.core.annotation.EventTarget;
import com.zhaomh.event.impl.GroupMessageEvent;
import com.zhaomh.id.UserId;
import com.zhaomh.message.MessageChain;
import com.zhaomh.plugin.BasePlugin;
import com.zhaomh.service.GroupService;
import com.zhaomh.service.MessageService;
import com.zhaomh.util.Messages;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class NoRepeatPlugin extends BasePlugin {
    private final int MAX_COUNT = 3;
    private final Map<UserId, Msg> userMsgs = new HashMap<>();

    private EventManager eventManager;

    @Getter
    @Setter
    @AllArgsConstructor
    private static class Msg {
        private String content;
        private int count;
    }

    @Override
    public void onLoad(PluginContext context) {
        eventManager = context.getService(EventManager.class);
    }

    @Override
    public void onEnable() {
        eventManager.register(this);
    }

    @Override
    public void onDisable() {
        eventManager.unregister(this);
    }

    @EventTarget
    public void onGroupMessage(GroupMessageEvent e) {
        String rw = e.getRawMessage();
        UserId userId = e.getSender().getUserId();
        Msg msg = userMsgs.getOrDefault(userId, new Msg("", 0));

        if (msg.getContent().equals(rw)) {
            msg.setCount(msg.getCount() + 1);
            if (msg.getCount() > MAX_COUNT) {
                e.getService(MessageService.class).sendGroupMessage(e.getGroupId(), MessageChain.of(Messages.at(userId), Messages.text("请勿刷屏！！！")));
                e.getService(GroupService.class).ban(e.getGroupId(), userId, 5*60);
                msg.setCount(0);
                msg.setContent("");
                return;
            }
        } else {
            msg = new Msg(rw, 1);
        }

        userMsgs.put(userId, msg);
    }
}
