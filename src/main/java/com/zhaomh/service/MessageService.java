package com.zhaomh.service;

import com.zhaomh.id.GroupId;
import com.zhaomh.id.UserId;
import com.zhaomh.message.MessageChain;

public interface MessageService {

    void sendGroupMessage(GroupId groupId, MessageChain msg);
    void sendPrivateMessage(UserId userId, MessageChain msg);
}
