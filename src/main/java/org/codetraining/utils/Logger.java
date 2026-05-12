package org.codetraining.utils;

/**
 * Compact logger: prints thread name and timestamp.
 * Supports different log levels and can be enabled/disabled.
 */
public final class Logger {
    private static volatile boolean logEnabled = true;

    private Logger() {
        // Prevent instantiation
    }

    /**
     * Logs a message with the current timestamp and thread name.
     *
     * @param fmt Format string (printf-style)
     * @param args Arguments for the format string
     */
    public static void log(String fmt, Object... args) {
        if (!logEnabled) {
            return;
        }

        String message = (args == null || args.length == 0) ? fmt : String.format(fmt, args);
        Thread current = Thread.currentThread();
        String logFormat = "[%tT.%1$tL][thread:%s] %s%n";
        System.out.printf(logFormat, System.currentTimeMillis(), current.getName(), message);
    }

    /**
     * Logs an info-level message.
     *
     * @param fmt Format string (printf-style)
     * @param args Arguments for the format string
     */
    public static void info(String fmt, Object... args) {
        logWithLevel("INFO", fmt, args);
    }

    /**
     * Logs an error-level message.
     *
     * @param fmt Format string (printf-style)
     * @param args Arguments for the format string
     */
    public static void error(String fmt, Object... args) {
        logWithLevel("ERROR", fmt, args);
    }

    /**
     * Disables all logging output.
     */
    public static void disablePrintLogs() {
        logEnabled = false;
    }

    /**
     * Enables logging output.
     */
    public static void enablePrintLogs() {
        logEnabled = true;
    }

    /**
     * Logs a message with the specified level prefix.
     *
     * @param level Log level (e.g., "INFO", "ERROR")
     * @param fmt Format string (printf-style)
     * @param args Arguments for the format string
     */
    private static void logWithLevel(String level, String fmt, Object... args) {
        String finalFmt = "[%s] %s".formatted(level.toUpperCase(), fmt);
        log(finalFmt, args);
    }
}
