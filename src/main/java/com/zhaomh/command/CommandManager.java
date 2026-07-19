package com.zhaomh.command;

import com.zhaomh.config.BotConfig;
import com.zhaomh.context.CommandContext;
import com.zhaomh.core.Accessor;
import com.zhaomh.core.annotation.Command;
import com.zhaomh.dto.EventSender;
import com.zhaomh.id.GroupId;
import com.zhaomh.event.EventManager;
import com.zhaomh.core.annotation.EventTarget;
import com.zhaomh.event.impl.GroupMessageEvent;
import com.zhaomh.event.impl.MessageEvent;
import com.zhaomh.event.impl.PrivateMessageEvent;
import com.zhaomh.logger.Logger;
import com.zhaomh.logger.LoggerFactory;
import com.zhaomh.message.AtSegment;
import com.zhaomh.message.MessageChain;
import com.zhaomh.message.MessageSegment;
import com.zhaomh.message.TextSegment;
import com.zhaomh.service.MessageService;
import com.zhaomh.service.PermissionService;
import com.zhaomh.util.Messages;
import com.zhaomh.util.StringUtil;
import com.zhaomh.util.TrieRouter;

import java.lang.reflect.Method;
import java.util.*;

public class CommandManager {
    private final Logger log = LoggerFactory.getLogger(CommandManager.class);

    private final TrieRouter<CommandMeta> normalCommands = new TrieRouter<>();
    private final TrieRouter<CommandMeta> actionCommands = new TrieRouter<>();

    private final Accessor accessor;
    private final BotConfig botConfig;

    public CommandManager(EventManager eventManager, Accessor accessor, BotConfig botConfig) {
        this.accessor = accessor;
        this.botConfig = botConfig;
        eventManager.register(this);
    }

    public void register(Object obj) {
        for (Method method : obj.getClass().getDeclaredMethods()) {
            method.setAccessible(true);
            if (method.isAnnotationPresent(Command.class)) {
                Command commandAnnotation = method.getAnnotation(Command.class);
                String commandName = commandAnnotation.value();
                CommandMeta meta = new CommandMeta(method, commandName, CommandType.NORMAL, obj);
                normalCommands.put(commandName.split("/"), meta);
                log.debug("注册普通命令: {}", commandName);
            } else if (method.isAnnotationPresent(ActionCommand.class)) {
                ActionCommand commandAnnotation = method.getAnnotation(ActionCommand.class);
                String commandName = commandAnnotation.value();
                CommandMeta meta = new CommandMeta(method, commandName, CommandType.ACTION, obj);
                actionCommands.put(commandName.split("/"), meta);
                log.debug("注册行为命令: {}", commandName);
            }
        }
    }

    public void unregister(Object obj) {
        for (Method method : obj.getClass().getDeclaredMethods()) {
            method.setAccessible(true);
            if (method.isAnnotationPresent(Command.class)) {
                Command commandAnnotation = method.getAnnotation(Command.class);
                String commandName = commandAnnotation.value();
                String[] path = commandName.split("/");
                normalCommands.remove(path);
                log.debug("取消注册普通命令: {}", commandName);
            } else if (method.isAnnotationPresent(ActionCommand.class)) {
                ActionCommand commandAnnotation = method.getAnnotation(ActionCommand.class);
                String commandName = commandAnnotation.value();
                String[] path = commandName.split("/");
                actionCommands.remove(path);
                log.debug("取消注册行为命令: {}", commandName);
            }
        }
    }

    public void onMessage(MessageEvent e, GroupId groupId) {
        String msg = e.getMessage().getPlainText();
        List<MessageSegment> segments = e.getMessage().getSegments();
        if (msg == null || msg.isBlank()) {
            return;
        }

        MessageSegment first = segments.getFirst();
        Map.Entry<CommandMeta, String[]> commandMetaEntry;
        String[] args;

        if (first instanceof AtSegment as) {
            segments.removeFirst();
            args = parseArgs(segments);
            commandMetaEntry = matchCommand(args, actionCommands);
            if (commandMetaEntry == null && !as.getTargetId().equals(botConfig.getBotId())) {
                return;
            }else {
                commandMetaEntry = matchCommand(args, normalCommands);
            }
        } else {
            args = parseArgs(segments);
            commandMetaEntry = matchCommand(args, normalCommands);
        }

        if (commandMetaEntry == null)
            return;

        CommandMeta matchedMeta = commandMetaEntry.getKey();
        String[] params = commandMetaEntry.getValue();
        String commandName = matchedMeta.getCommand();
        EventSender sender = e.getSender();

        RequirePermission requirePermission = matchedMeta.getMethod().getAnnotation(RequirePermission.class);
        if (requirePermission != null && !accessor.getService(PermissionService.class).hasPermission(sender.getUserId(), requirePermission.value())) {
            if (groupId != null) {
                accessor.getService(MessageService.class).sendGroupMessage(groupId, MessageChain.of(Messages.at(sender.getUserId()),Messages.text(" 你没有权限执行该命令！")));
            } else {
                accessor.getService(MessageService.class)   .sendPrivateMessage(sender.getUserId(), MessageChain.text("你没有权限执行该命令！"));
            }
            log.debug("{} 没有执行 {} 的权限", sender.getDisplayName(), commandName);
            return;
        }

        try {
            matchedMeta.getMethod().invoke(matchedMeta.getInstance(),new CommandContext(accessor,
                    e.getMessage(),
                    commandName,
                    params,
                    sender,
                    groupId,
                    e.getMessageId()
                    ));

        } catch (Exception ex) {
            if (groupId != null) {
                accessor.getService(MessageService.class).sendGroupMessage(groupId, MessageChain.of(Messages.at(sender.getUserId()),Messages.text(" 执行命令 " + commandName + " 时发生了内部错误")));
            } else {
                accessor.getService(MessageService.class).sendPrivateMessage(sender.getUserId(), MessageChain.text("执行命令 " + commandName + " 时发生了内部错误"));
            }
        }
    }

    private Map.Entry<CommandMeta, String[]> matchCommand(String[] args, TrieRouter<CommandMeta> router) {
        if (args.length == 0) {
            return null;
        }

        CommandMeta matchedMeta = null;
        int matchLen = 0;

        for (int len = args.length; len > 0; len--) {
            String[] path = Arrays.copyOfRange(args, 0, len);
            CommandMeta meta = router.get(path);
            if (meta != null) {
                matchedMeta = meta;
                matchLen = len;
                break;
            }
        }

        if (matchedMeta == null) {
            return null;
        }

        return new AbstractMap.SimpleEntry<>(matchedMeta, Arrays.copyOfRange(args, matchLen, args.length));
    }

    @EventTarget
    public void onPrivateMessage(PrivateMessageEvent e){
        onMessage(e, null);
    }

    @EventTarget
    public void onGroupMessage(GroupMessageEvent e) {
        onMessage(e, e.getGroupId());
    }

    private String[] parseArgs(List<MessageSegment> segments) {
        segments.replaceAll(segment -> {
            if (segment instanceof TextSegment textSegment) {
                return new TextSegment(textSegment.getPlainText().replaceAll("\n", " "));
            }
            return segment;
        });
        StringBuilder sb = new StringBuilder();
        MessageSegment lastSegment = null;
        for (MessageSegment segment : segments) {
            if (lastSegment != null && lastSegment.getType() != null && lastSegment.getType().equals(segment.getType())) {
                sb.append(segment.getPlainText());
                lastSegment = segment;
                continue;
            }
            if (lastSegment instanceof TextSegment ts) {
                if (!ts.getPlainText().endsWith(" "))
                    sb.append(" ");
            } else {
                sb.append(" ");
            }
            lastSegment = segment;
            sb.append(segment.getPlainText());
        }
        String text = sb.toString().trim();

        return StringUtil.split(text);
    }

    public List<String> getCommandNames() {
        List<String> names = new ArrayList<>();
        // 从两个 TrieRouter 中提取所有命令路径
        names.addAll(normalCommands.getAllKeys());
        names.addAll(actionCommands.getAllKeys());
        return names;
    }

    public int getCommandCount() {
        return normalCommands.size() + actionCommands.size();
    }
}
