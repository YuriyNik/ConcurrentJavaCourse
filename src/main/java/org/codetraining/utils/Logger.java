package org.codetraining.utils;

public class Logger {
    private Logger() {}
    public static void log(String fmt, Object... args) {
        String message = (args == null || args.length == 0) ? fmt :
                String.format(fmt, args);
        System.out.printf("[%tT.%1$tL][thread:%s] %s%n",
                System.currentTimeMillis(),
                Thread.currentThread().getName(),
                message);
    }
}
