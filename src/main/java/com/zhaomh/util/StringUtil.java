package com.zhaomh.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class StringUtil {
    public static String format(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }

        // 检测最后一个参数是否为 Throwable，若是则将其视为异常单独处理
        Object lastArg = args[args.length - 1];
        boolean hasThrowable = (lastArg instanceof Throwable);
        int msgArgCount = hasThrowable ? args.length - 1 : args.length;

        StringBuilder sb = new StringBuilder();
        int argIndex = 0;
        int pos = 0;
        int length = message.length();
        while (pos < length) {
            int idx = message.indexOf("{}", pos);
            if (idx == -1) {
                sb.append(message, pos, length);
                break;
            }
            sb.append(message, pos, idx);
            if (argIndex < msgArgCount) {
                sb.append(args[argIndex]);
                argIndex++;
            } else {
                sb.append("{}");
            }
            pos = idx + 2;
        }

        // 如果有 Throwable 参数，追加堆栈信息
        if (hasThrowable) {
            sb.append(System.lineSeparator()).append(getStackTrace((Throwable) lastArg));
        }
        return sb.toString();
    }

    public static String getStackTrace(Throwable t) {
        if (t == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    /**
     * 中文：分割字符串，支持带引号的字符串
     * 例子：a b c -> ["a", "b", "c"]
     * a b c -> ["a", "b", "c"]
     * a "b" c -> ["a", "b", "c"]
     * a "b c" d -> ["a", "b c", "d"]
     * @param text
     * @return
     */
    public static String[] split(String text) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;

        for (char c : text.toCharArray()) {
            if (inQuotes) {
                if (c == quoteChar) { inQuotes = false; }
                else { current.append(c); }
            } else {
                if (c == '"' || c == '\'') { inQuotes = true; quoteChar = c; }
                else if (c == ' ') {
                    if (!current.isEmpty()) { result.add(current.toString()); current.setLength(0); }
                } else { current.append(c); }
            }
        }
        if (!current.isEmpty()) result.add(current.toString());
        return result.toArray(new String[0]);
    }
}
