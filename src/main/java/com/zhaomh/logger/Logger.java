package com.zhaomh.logger;

import com.zhaomh.util.StringUtil;
import com.zhaomh.web.LoggingWebSocketServer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.locks.ReentrantLock;

public class Logger {
    public static final int ERROR = 4;
    public static final int WARN = 3;
    public static final int INFO = 2;
    public static final int DEBUG = 1;

    // 线程安全：volatile 保证可见性
    public static volatile int level = DEBUG;

    private static final ReentrantLock lock = new ReentrantLock();
    private final String name;

    // 使用 ThreadLocal 避免 SimpleDateFormat 线程安全问题，且复用对象
    private static final ThreadLocal<DateFormat> timeFormatter =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("HH:mm:ss:SSSS"));

    private static final File logsDir = new File("logs");
    private static BufferedWriter writer;
    private static String currentDateStr = "";

    static {
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }
        ensureRollFile(new Date());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            lock.lock();
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            } catch (IOException ignored) {
            } finally {
                lock.unlock();
            }
        }));
    }

    private static void ensureRollFile(Date date) {
        String newDateStr = new SimpleDateFormat("yyyy-MM-dd").format(date);
        // 快速无锁检查，避免大多数情况下的锁竞争
        if (newDateStr.equals(currentDateStr)) {
            return;
        }
        lock.lock();
        try {
            // 双重检查，防止重复创建
            if (!newDateStr.equals(currentDateStr)) {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
                writer = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(new File(logsDir, newDateStr + ".log"), true),
                                StandardCharsets.UTF_8
                        )
                );
                currentDateStr = newDateStr;
            }
        } catch (IOException e) {
            System.err.println("Failed to roll log file: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public Logger(Class<?> clazz) {
        this.name = clazz.getName();
    }

    private static void writeLog(String level, String name, String msg) {
        ensureRollFile(new Date());
        lock.lock();
        try {
            if (LoggingWebSocketServer.isReady()) {  // 你自己加个静态标记
                LoggingWebSocketServer.broadcast(level, msg);
            }
            msg = nowTimeStr() + " [" + level + "] " + name + ": "+msg;
            System.out.println(msg);
            if (writer != null) {
                writer.write(msg);
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            System.err.println("Cannot write log: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    private static String nowTimeStr() {
        timeFormatter.get().setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return timeFormatter.get().format(new Date());
    }

    public void info(String str, Object... args) {
        if (INFO >= level) {
            writeLog("INFO", name, StringUtil.format(str, args));
        }
    }

    public void debug(String str, Object... args) {
        if (DEBUG >= level) {
            writeLog("DEBUG", name, StringUtil.format(str, args));
        }
    }

    public void warn(String str, Object... args) {
        if (WARN >=  level) {
            writeLog("WARN", name, StringUtil.format(str, args));
        }
    }

    public void error(String str, Object... args) {
        if (ERROR  >= level) {
            writeLog("ERROR", name, StringUtil.format(str, args));
        }
    }

    public void error(Throwable e) {
        if (ERROR  >= level) {
            writeLog("ERROR", name, StringUtil.getStackTrace(e));
        }
    }

    public void error(String msg, Throwable e) {
        if (ERROR  >= level) {
            writeLog("ERROR", name,  msg + " " + StringUtil.getStackTrace(e));
        }
    }
}