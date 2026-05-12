package org.codetraining.utils;

import java.security.SecureRandom;

/**
 * Utility class for safe thread sleeping operations.
 * All methods properly handle InterruptedException by restoring the interrupt flag.
 */
public final class ThreadSleepUtil {
    private static final SecureRandom RANDOM = new SecureRandom();

    private ThreadSleepUtil() {
        // Prevent instantiation
    }

    /**
     * Sleeps for the specified number of milliseconds.
     * If interrupted, restores the interrupt flag and returns.
     *
     * @param ms Number of milliseconds to sleep
     */
    public static void safeSleepWithoutThrow(long ms) {
        if (ms <= 0) {
            return;
        }

        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Sleeps for approximately the specified milliseconds with random jitter.
     * The actual sleep time can be ±50% of the requested time due to random variation.
     * If interrupted, restores the interrupt flag and returns.
     *
     * @param ms Number of milliseconds to sleep (with jitter applied)
     */
    public static void safeSleepWithJitter(long ms) {
        if (ms <= 0) {
            return;
        }

        try {
            // Calculate random jitter: ±50% of requested time
            long jitter = RANDOM.nextLong(ms / 2);

            // Randomly positive or negative jitter
            if (RANDOM.nextBoolean()) {
                jitter *= -1;
            }

            Thread.sleep(ms + jitter);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Sleeps for a random duration within the specified range.
     * If interrupted, restores the interrupt flag and returns.
     *
     * @param lowerBound Minimum sleep time in milliseconds (inclusive)
     * @param upperBound Maximum sleep time in milliseconds (exclusive)
     */
    public static void safeSleepRandomMillis(long lowerBound, long upperBound) {
        try {
            long sleepMillis = RANDOM.nextLong(lowerBound, upperBound);
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
