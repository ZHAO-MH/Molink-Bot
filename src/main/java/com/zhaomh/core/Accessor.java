package com.zhaomh.core;

public interface Accessor {
    <T> T getService(Class<T> type);
    <T> boolean hasService(Class<T> type);
}