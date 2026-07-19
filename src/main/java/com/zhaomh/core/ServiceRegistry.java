package com.zhaomh.core;

public interface ServiceRegistry {
    // 注册服务（通常由 builtin 插件或外部插件调用）
    <T> void register(Class<T> type, T instance);

    // 获取服务（由 Command/Plugin 调用）
    <T> T getService(Class<T> type);

    // 检查是否存在
    <T> boolean hasService(Class<T> type);
}