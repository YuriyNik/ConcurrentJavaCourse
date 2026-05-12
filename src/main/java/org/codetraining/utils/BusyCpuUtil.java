package org.codetraining.utils;

import java.security.SecureRandom;

/**
 * Utility class to simulate CPU-intensive work (busy waiting).
 * Useful for testing concurrent scenarios and measuring performance under CPU load.
 */
public final class BusyCpuUtil {
    private static final SecureRandom RANDOM = new SecureRandom();

    private BusyCpuUtil() {
        // Prevent instantiation
    }

    /**
     * Spins on CPU for the specified number of milliseconds.
     * Performs meaningless arithmetic to keep the CPU busy (prevents JIT optimization).
     *
     * @param ms Number of milliseconds to keep the CPU busy
     */
    public static void spinOnCpuMillis(long ms) {
        long end = System.nanoTime() + ms * 1_000_000L;
        long acc = 0;

        // Busy loop: keep CPU working until time is up
        while (System.nanoTime() < end) {
            acc += (acc * 31 + 7); // Some useless arithmetic to prevent JIT optimization
        }

        // Anti-optimization: prevent JIT from optimizing away the entire loop
        if (acc == 42) {
            System.out.print("");
        }
    }

    /**
     * Spins on CPU for approximately the specified milliseconds, with random jitter.
     * The actual spin time can be ±50% of the requested time due to random variation.
     * Useful for simulating variable workload and non-deterministic timing.
     *
     * @param ms Number of milliseconds to keep the CPU busy (with jitter applied)
     */
    public static void spinOnCpuMillisWithJitter(long ms) {
        if (ms <= 0) {
            return;
        }

        // Calculate random jitter: ±50% of requested time
        long jitter = RANDOM.nextLong(ms / 2);

        // Randomly positive or negative jitter
        if (RANDOM.nextBoolean()) {
            jitter *= -1;
        }

        // Spin with adjusted time
        spinOnCpuMillis(ms + jitter);
    }
}
