package com.lizi.skyright;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogManager {

    private static final int MAX_SIZE = 1000;
    private static final Queue<LogEntry> logBuffer = new ConcurrentLinkedQueue<>();
    private static final ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault());
        }
    };

    private LogManager() {}

    private static class LogEntry {
        final String time;
        final String tag;
        final String msg;

        LogEntry(String time, String tag, String msg) {
            this.time = time;
            this.tag = tag;
            this.msg = msg;
        }

        @Override
        public String toString() {
            return time + " | " + tag + " | " + msg;
        }
    }

    public static void log(String tag, String msg) {
        // 使用 formatter.get() 获取当前线程的 SimpleDateFormat 实例
        String time = formatter.get().format(new Date());
        LogEntry entry = new LogEntry(time, tag, msg);
        logBuffer.offer(entry);
        if (logBuffer.size() > MAX_SIZE) {
            logBuffer.poll();
        }
    }

    public static void clearLog() {
        logBuffer.clear();
    }

    public static List<String> getLogsByTag(String tag) {
        List<String> result = new ArrayList<>();
        for (LogEntry entry : logBuffer) {
            if (tag.equals(entry.tag)) {
                result.add(entry.toString());
            }
        }
        return result;
    }

    public static List<String> getLogsExcludeTag(String tag) {
        List<String> result = new ArrayList<>();
        for (LogEntry entry : logBuffer) {
            if (!tag.equals(entry.tag)) {  // 关键：排除指定 tag
                result.add(entry.toString());
            }
        }
        return result;
    }


    public static List<String> getAllLogs() {
        List<String> result = new ArrayList<>();
        for (LogEntry entry : logBuffer) {
            result.add(entry.toString());
        }
        return result;
    }
}

