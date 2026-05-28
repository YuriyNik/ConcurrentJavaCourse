package org.codetraining;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Rate limiter using ReentrantLock instead of synchronized.
 * Allows 100 requests per user per 1 second window (sliding window algorithm).
 */
public class RateLimiter{
    private static final int MAX_REQUESTS = 100;
    private static final long WINDOW = 1000;

    // Store for timestamps per user
    private final Map<String, Deque<Long>> store = new ConcurrentHashMap<>();

    // Per-user locks for better concurrency control than synchronized
    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    /**
     * Check if a request from the given user is allowed.
     * Uses ReentrantLock for thread-safe access instead of synchronized keyword.
     *
     * @param userId The user making the request
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean allowRequest(String userId){
        long now = System.currentTimeMillis();

        // Get or create a lock for this user
        ReentrantLock lock = locks.computeIfAbsent(userId, id -> new ReentrantLock());

        // Acquire lock - equivalent to synchronized (userId)
        lock.lock();
        try {
            // Critical section: check and update rate limit
            Deque<Long> timestamps = store.computeIfAbsent(userId,
                    id -> new ArrayDeque<>()
            );
            cleanupOldRequests(timestamps, now);
            if (timestamps.size() >= MAX_REQUESTS) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        } finally {
            // Always release lock (equivalent to exiting synchronized block)
            lock.unlock();
        }
    }
    /**
     * Remove timestamps older than the current window.
     * Cleans up old requests to prevent memory growth.
     *
     * @param timestamps The deque of timestamps for a user
     * @param now The current time in milliseconds
     */
    private void cleanupOldRequests(Deque<Long> timestamps, long now) {
        long threshold = now - WINDOW;
        while (!timestamps.isEmpty() && timestamps.peek() < threshold) {
            timestamps.removeFirst();
        }
    }
}
