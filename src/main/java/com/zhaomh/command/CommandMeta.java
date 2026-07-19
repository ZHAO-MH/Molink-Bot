package com.zhaomh.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;

@Getter
@AllArgsConstructor
public class CommandMeta {
    private final Method method;
    private final String command;
    private final CommandType type;

    private final Object instance;
}
