package com.zhaomh.service;

public interface StatusService {
    long getStartTime();

    long getUptimeMillis();

    String getUptimeFormatted();

    long getUsedMemoryMB();

    long getMaxMemoryMB();

    boolean isBotConnected();
}