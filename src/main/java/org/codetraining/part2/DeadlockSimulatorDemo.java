package org.codetraining.part2;

import org.codetraining.utils.Logger;
import java.util.concurrent.TimeUnit;
import java.util.Random;

public class DeadlockSimulatorDemo {
    /**
     * Demonstrates deadlock between two threads acquiring resources in different order.
     * Thread-1: r1 -> r2
     * Thread-2: r2 -> r1
     *
     * On some iterations, both threads hang waiting for each other.
     */
    public static void main(String[] args) throws InterruptedException {
        Logger.log("=== DeadlockSimulatorDemo START ===");

        // ========== STEP 1: Demonstrating deadlock (WrongLockableResourceProcessor) ==========
//        demonstrateBuggyVersion();

        // ========== STEP 2: Demonstrating fix (OrderedLockableResourceProcessor) ==========
        Logger.log("\n");
//        demonstrateFixedVersion();

        // ========== STEP 3: Demonstrating fix #2 (TryLockLockableResourceProcessor) ==========
        Logger.log("\n");
        demonstrateTryLockVersion();

        Logger.log("=== DeadlockSimulatorDemo END ===");
    }

    /**
     * Demonstrates deadlock with WrongLockableResourceProcessor.
     */
    static void demonstrateBuggyVersion() throws InterruptedException {
        Logger.log(">>> DEMONSTRATING DEADLOCK (Buggy) <<<");

        LockableResource resourceA = new LockableResource("Resource-A", 1);
        LockableResource resourceB = new LockableResource("Resource-B", 2);

        WrongLockableResourceProcessor processor = new WrongLockableResourceProcessor();

        // Thread-1: will acquire r1 (A), then r2 (B)
        Thread thread1 = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    Logger.log("Iteration %d: Thread-1 starts processResources(A, B)", i + 1);
                    processor.processResources(resourceA, resourceB);
                    Logger.log("Iteration %d: Thread-1 finished processResources(A, B)", i + 1);
                    Thread.sleep(100); // Pause between iterations
                }
            } catch (InterruptedException e) {
                Logger.log("Thread-1 interrupted: %s", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }, "Thread-1");

        // Thread-2: will acquire r2 (B), then r1 (A) — opposite order!
        Thread thread2 = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    Logger.log("Iteration %d: Thread-2 starts processResources(B, A)", i + 1);
                    processor.processResources(resourceB, resourceA);
                    Logger.log("Iteration %d: Thread-2 finished processResources(B, A)", i + 1);
                    Thread.sleep(100); // Pause between iterations
                }
            } catch (InterruptedException e) {
                Logger.log("Thread-2 interrupted: %s", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }, "Thread-2");

        thread1.start();
        thread2.start();

        // Give threads time to work and hang
        Logger.log("Main thread: waiting 3 seconds...");
        Thread.sleep(3000);

        Logger.log("Checking if threads are alive:");
        Logger.log("thread1.isAlive() = %s", thread1.isAlive());
        Logger.log("thread2.isAlive() = %s", thread2.isAlive());

        if (thread1.isAlive() || thread2.isAlive()) {
            Logger.log("❌ DEADLOCK DETECTED! Threads are deadlocked.");
        } else {
            Logger.log("✓ No deadlock this time (luck or not enough iterations)");
        }

        // Try to join with timeout (won't work if there's a deadlock)
        Logger.log("Trying to join threads...");
        thread1.join(2000); // 2 sec timeout
        thread2.join(2000);

        if (thread1.isAlive() || thread2.isAlive()) {
            Logger.log("❌ Threads did not finish (DEADLOCK confirmed)");
        } else {
            Logger.log("✓ Threads completed their work");
        }
    }

    /**
     * Demonstrates deadlock fix with OrderedLockableResourceProcessor.
     */
    static void demonstrateFixedVersion() throws InterruptedException {
        Logger.log(">>> DEMONSTRATING FIX (Ordered, Fix #1) <<<");

        LockableResource resourceA = new LockableResource("Resource-A", 1);
        LockableResource resourceB = new LockableResource("Resource-B", 2);

        OrderedLockableResourceProcessor processor = new OrderedLockableResourceProcessor();

        // Thread-1: will acquire r1 (A), then r2 (B)
        Thread thread1 = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    Logger.log("Iteration %d: Thread-1 starts processResources(A, B)", i + 1);
                    processor.processResources(resourceA, resourceB);
                    Logger.log("Iteration %d: Thread-1 finished processResources(A, B)", i + 1);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Logger.log("Thread-1 interrupted: %s", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }, "Thread-1");

        // Thread-2: will acquire r2 (B), then r1 (A) — but will acquire in correct order!
        Thread thread2 = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    Logger.log("Iteration %d: Thread-2 starts processResources(B, A)", i + 1);
                    processor.processResources(resourceB, resourceA);
                    Logger.log("Iteration %d: Thread-2 finished processResources(B, A)", i + 1);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Logger.log("Thread-2 interrupted: %s", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }, "Thread-2");

        thread1.start();
        thread2.start();

        // Wait for all threads to complete
        Logger.log("Main thread: waiting for threads to finish...");
        thread1.join();
        thread2.join();

        Logger.log("✓✓✓ Both threads successfully completed all 5 iterations WITHOUT DEADLOCK!");
        Logger.log("✓ OrderedLockableResourceProcessor works correctly!");
    }

    /**
     * Demonstrates deadlock fix #2 with TryLockLockableResourceProcessor.
     * Uses tryLock() with timeout and exponential backoff strategy.
     */
    static void demonstrateTryLockVersion() throws InterruptedException {
        Logger.log(">>> DEMONSTRATING FIX #2 (TryLock + Backoff) <<<");

        LockableResource resourceA = new LockableResource("Resource-A", 1);
        LockableResource resourceB = new LockableResource("Resource-B", 2);

        TryLockLockableResourceProcessor processor = new TryLockLockableResourceProcessor();

        // Thread-1: will try to acquire r1 (A), then r2 (B) with retries
        Thread thread1 = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    Logger.log("Iteration %d: Thread-1 starts processResources(A, B)", i + 1);
                    processor.processResources(resourceA, resourceB);
                    Logger.log("Iteration %d: Thread-1 finished processResources(A, B)", i + 1);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Logger.log("Thread-1 interrupted: %s", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }, "Thread-1");

        // Thread-2: will try to acquire r2 (B), then r1 (A) with retries
        Thread thread2 = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    Logger.log("Iteration %d: Thread-2 starts processResources(B, A)", i + 1);
                    processor.processResources(resourceB, resourceA);
                    Logger.log("Iteration %d: Thread-2 finished processResources(B, A)", i + 1);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Logger.log("Thread-2 interrupted: %s", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }, "Thread-2");

        thread1.start();
        thread2.start();

        // Wait for all threads to complete
        Logger.log("Main thread: waiting for threads to finish...");
        thread1.join();
        thread2.join();

        Logger.log("✓✓✓ Both threads successfully completed all 5 iterations WITHOUT DEADLOCK!");
        Logger.log("✓ TryLockLockableResourceProcessor works correctly with retries and backoff!");
    }
}


/**
 * LockableResourceProcessor implementation that demonstrates deadlock.
 *
 * Acquires resources in order r1 -> r2:
 * - If one thread acquires r1 and another acquires r2,
 *   and both try to acquire the second resource,
 *   circular mutual blocking (deadlock) will occur.
 */
class WrongLockableResourceProcessor implements LockableResourceProcessor {
    @Override
    public void processResources(LockableResource r1, LockableResource r2) throws InterruptedException {
        Logger.log("Attempting to acquire %s (id=%d)", r1.getName(), r1.getId());

        synchronized (r1.getMonitor()) {
            Logger.log("✓ Acquired %s", r1.getName());

            // Insert pause to increase chance of mutual blocking
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                Logger.log("⚠ Interrupted during sleep()");
                Thread.currentThread().interrupt();
                throw e;
            }

            Logger.log("Attempting to acquire %s (already holding %s)", r2.getName(), r1.getName());

            synchronized (r2.getMonitor()) {
                Logger.log("✓ Acquired %s. Now I have both: %s and %s", r2.getName(), r1.getName(), r2.getName());

                // Inside critical section over both resources
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Logger.log("⚠ Interrupted during critical section");
                    Thread.currentThread().interrupt();
                    throw e;
                }

                Logger.log("Exiting critical section (releasing %s)", r2.getName());
            }

            Logger.log("Exiting critical section (releasing %s)", r1.getName());
        }
    }
}

/**
 * Fix #1: OrderedLockableResourceProcessor — ordered acquisition.
 *
 * Idea: always acquire resources in one order (by ID).
 * This eliminates circular blocking because both threads will
 * acquire resources in the same sequence.
 *
 * Example:
 *   Thread-1: processResources(A, B) -> acquires A (id=1), then B (id=2)
 *   Thread-2: processResources(B, A) -> acquires A (id=1), then B (id=2)
 *            (despite parameter order!)
 *
 * Result: deadlock eliminated!
 */
class OrderedLockableResourceProcessor implements LockableResourceProcessor {
    @Override
    public void processResources(LockableResource r1, LockableResource r2) throws InterruptedException {
        // Sort resources by ID: always acquire in one order
        LockableResource first, second;
        if (r1.getId() < r2.getId()) {
            first = r1;
            second = r2;
        } else {
            first = r2;
            second = r1;
        }

        Logger.log("Attempting to acquire first resource: %s (id=%d)", first.getName(), first.getId());

        synchronized (first.getMonitor()) {
            Logger.log("✓ Acquired %s", first.getName());

            // Insert pause to simulate real conditions
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                Logger.log("⚠ Interrupted during sleep()");
                Thread.currentThread().interrupt();
                throw e;
            }

            Logger.log("Attempting to acquire second resource: %s (already holding %s)", second.getName(), first.getName());

            synchronized (second.getMonitor()) {
                Logger.log("✓ Acquired %s. Now I have both: %s and %s", second.getName(), first.getName(), second.getName());

                // Inside critical section over both resources
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Logger.log("⚠ Interrupted during critical section");
                    Thread.currentThread().interrupt();
                    throw e;
                }

                Logger.log("Exiting critical section (releasing %s)", second.getName());
            }

            Logger.log("Exiting critical section (releasing %s)", first.getName());
        }
    }
}

/**
 * Fix #2: TryLockLockableResourceProcessor — tryLock with timeout and backoff.
 *
 * Idea: instead of unconditional lock acquisition, use tryLock(timeout).
 * If unable to acquire a lock:
 *   1. Release all acquired locks
 *   2. Apply random backoff (10-30 ms)
 *   3. Retry from the beginning
 *
 * Important: Uses only ReentrantLock because Object.monitor() has no tryLock() method.
 *
 * Benefits:
 *   - Prevents circular waiting (deadlock)
 *   - Shows retries and backoff in logs
 *   - More realistic simulation of real-world scenarios
 *
 * Algorithm:
 *   while (!acquired) {
 *     1. Sort resources by ID (total order)
 *     2. Try to acquire first lock with timeout
 *     3. If success, try to acquire second lock with timeout
 *     4. If both acquired, do work and set acquired=true
 *     5. On failure at any step:
 *        - Release all held locks
 *        - Log retry info
 *        - Sleep(random backoff)
 *        - Continue loop
 *   }
 */
class TryLockLockableResourceProcessor implements LockableResourceProcessor {
    // Lock acquisition timeout (in milliseconds)
    private static final long LOCK_TIMEOUT_MS = 10;

    // Backoff range for exponential backoff
    private static final long BACKOFF_MIN_MS = 25;
    private static final long BACKOFF_MAX_MS = 50;

    // Shared random instance for backoff calculation
    private static final Random RANDOM = new Random();

    @Override
    public void processResources(LockableResource r1, LockableResource r2) throws InterruptedException {
        // Sort resources by ID to ensure consistent ordering (same as Fix #1)
        LockableResource first, second;
        if (r1.getId() < r2.getId()) {
            first = r1;
            second = r2;
        } else {
            first = r2;
            second = r1;
        }

        boolean acquired = false;
        int retries = 0;
        int maxRetries = 10; // Prevent infinite loops in edge cases

        while (!acquired && retries < maxRetries) {
            // Step 1: Try to acquire first lock with timeout
            if (first.getLock().tryLock(LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                try {
                    Logger.log("✓ Acquired first lock: %s (id=%d)", first.getName(), first.getId());

                    // Step 2: Try to acquire second lock with timeout
                    if (second.getLock().tryLock(LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                        try {
                            Logger.log("✓ Acquired second lock: %s (id=%d). Now I have both.", second.getName(), second.getId());
                            // Step 3: Successfully acquired both locks - do work
                            try {
                                // Insert pause to simulate real work
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                Logger.log("⚠ Interrupted during critical section work");
                                Thread.currentThread().interrupt();
                                throw e;
                            }

                            Logger.log("Exiting critical section (releasing both locks)");
                            acquired = true; // Mark success and exit loop

                        } finally {
                            // Always release second lock
                            second.getLock().unlock();
                            Logger.log("Released lock: %s", second.getName());
                        }
                    } else {
                        // Failed to acquire second lock - release first and retry
                        Logger.log("⚠ Failed to acquire second lock (%s), backing off (attempt %d)", second.getName(), retries + 1);
                        first.getLock().unlock();
                    }
                } finally {
                    // Always release first lock if we didn't acquire both
                    if (!acquired) {
                        first.getLock().unlock();
                        Logger.log("Released lock: %s (due to failure acquiring second lock)", first.getName());
                    }
                }
            } else {
                // Failed to acquire first lock immediately - log and retry
                Logger.log("⚠ Failed to acquire first lock (%s), backing off (attempt %d)", first.getName(), retries + 1);
            }

            // If not acquired, apply backoff and retry
            if (!acquired) {
                long backoffMs = BACKOFF_MIN_MS + RANDOM.nextInt((int) (BACKOFF_MAX_MS - BACKOFF_MIN_MS + 1));
                Logger.log("Backing off for %d ms before retry #%d", backoffMs, retries + 1);
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException e) {
                    Logger.log("⚠ Interrupted during backoff");
                    Thread.currentThread().interrupt();
                    throw e;
                }
                retries++;
            }
        }

        if (!acquired) {
            Logger.log("❌ Failed to acquire locks after %d attempts", maxRetries);
            throw new InterruptedException("Failed to acquire locks within max retries");
        }

        Logger.log("✓ Successfully completed critical section on %s and %s", first.getName(), second.getName());
    }
}
