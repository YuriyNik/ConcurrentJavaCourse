package org.codetraining.utils;

import java.util.function.Supplier;

public class MeasurementUtils {
    private MeasurementUtils() {}

    /**
     * Measures and logs the execution duration of a task (Supplier version with return value).
     *
     * @param <T> The return type of the task
     * @param taskName The name of the task for logging
     * @param task The task to measure (Supplier that returns a value)
     * @return The result of the task execution
     * @throws Exception If the task throws an exception
     */
    public static <T> T measure(String taskName, Supplier<T> task) {
        long startNanos = System.nanoTime();
        try {
            return task.get();
        } catch (Exception e) {
            Logger.log("Exception in task '%s': %s", taskName, e.getMessage());
            throw e;
        } finally {
            long endNanos = System.nanoTime();
            long timeElapsedMs = (endNanos - startNanos) / 1_000_000;
            Logger.log("Time: task '%s' completed in %d ms", taskName, timeElapsedMs);
        }
    }

    /**
     * Measures and logs the execution duration of a task (Runnable version with no return value).
     *
     * @param taskName The name of the task for logging
     * @param task The task to measure (Runnable)
     * @throws Exception If the task throws an exception
     */
    public static void measure(String taskName, Runnable task) {
        long startNanos = System.nanoTime();
        try {
            task.run();
        } catch (Exception e) {
            Logger.log("Exception in task '%s': %s", taskName, e.getMessage());
            throw e;
        } finally {
            long endNanos = System.nanoTime();
            long timeElapsedMs = (endNanos - startNanos) / 1_000_000;
            Logger.log("Time: task '%s' completed in %d ms", taskName, timeElapsedMs);
        }
    }
}
