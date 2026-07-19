package com.zhaomh.context;

import com.zhaomh.core.Accessor;
import com.zhaomh.dto.EventSender;
import com.zhaomh.id.GroupId;
import com.zhaomh.id.MessageId;
import com.zhaomh.message.MessageChain;
import com.zhaomh.message.MessageSegment;
import com.zhaomh.service.GroupService;
import com.zhaomh.service.MessageService;
import com.zhaomh.util.Messages;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommandContext {
    private final Accessor accessor;
    private final MessageChain message;
    private final String command;
    private final String[] args;
    private final EventSender sender;
    private final GroupId groupId;
    private final MessageId messageId;

    public boolean isInGroup() {
        return groupId != null;
    }

    public void sendMessage(String message) {
        if (isInGroup()) {
            accessor.getService(MessageService.class).sendGroupMessage(groupId, MessageChain.text(message));
        } else {
            accessor.getService(MessageService.class).sendPrivateMessage(sender.getUserId(), MessageChain.text(message));
        }
    }

    public void sendMessage(MessageChain message) {
        if (isInGroup()) {
            accessor.getService(MessageService.class).sendGroupMessage(groupId, message);
        } else {
            accessor.getService(MessageService.class).sendPrivateMessage(sender.getUserId(), message);
        }
    }

    /**
     * 回复消息，如果在群聊中，会在消息开头At用户
     */
    public void sendAtMessage(MessageChain message) {
        MessageChain replyMessage = MessageChain.of(Messages.at(sender.getUserId()), Messages.text(" "));
        for (MessageSegment segment : message.getSegments()) {
            replyMessage = replyMessage.append(segment);
        }

        sendMessage(replyMessage);
    }

    public void sendAtLnMessage(MessageChain message) {
        MessageChain replyMessage = MessageChain.of(Messages.at(sender.getUserId()), Messages.text("\n"));
        for (MessageSegment segment : message.getSegments()) {
            replyMessage = replyMessage.append(segment);
        }

        sendMessage(replyMessage);
    }

    public void sendAtMessage(String message) {
        sendAtMessage(MessageChain.text(message));
    }

    public void sendAtLnMessage(String message) {
        sendAtLnMessage(MessageChain.text(message));
    }

    public void sentReplyMessage(MessageChain message) {
        sendMessage(MessageChain.reply(messageId, message));
    }

    public void sentReplyMessage(String message) {
        sendMessage(MessageChain.reply(messageId, Messages.text(message)));
    }

    public <T> T getService(Class<T> type) {
        return accessor.getService(type);
    }
}