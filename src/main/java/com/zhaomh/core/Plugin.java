package com.zhaomh.core;

import com.zhaomh.context.PluginContext;
import lombok.Getter;

public interface Plugin {
    void onLoad(PluginContext context);

    void onUnload();

    void onEnable();

    void onDisable();

    boolean isEnabled();

    String getName();
}
