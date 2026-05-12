package org.codetraining.part3;

import org.codetraining.utils.Logger;
import org.codetraining.utils.ThreadSleepUtil;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe storage for story results.
 * Simulates database-like operations with I/O delays.
 */
public final class StoryStorage {
    // Simulate I/O delay for storage operations
    private static final int IO_DELAY_MS = 50;

    // Thread-safe storage using ConcurrentHashMap
    private final Map<Long, StoryResult> storage = new ConcurrentHashMap<>();

    /**
     * Saves a story result to storage.
     * Simulates I/O delay for database write operation.
     *
     * @param result The story result to save
     */
    public void save(StoryResult result) {
        Logger.log("Saving result for task %d", result.taskId());

        // Simulate I/O delay
        ThreadSleepUtil.safeSleepWithoutThrow(IO_DELAY_MS);

        // Upsert operation (insert or update)
        storage.put(result.taskId(), result);

        Logger.log("Result saved successfully for task %d", result.taskId());
    }

    /**
     * Retrieves a story result by task ID.
     * Simulates I/O delay for database read operation.
     *
     * @param taskId The task ID to look up
     * @return An Optional containing the result if found, empty otherwise
     */
    public Optional<StoryResult> get(long taskId) {
        Logger.log("Retrieving result for task %d", taskId);

        // Simulate I/O delay
        ThreadSleepUtil.safeSleepWithoutThrow(IO_DELAY_MS);

        StoryResult result = storage.get(taskId);

        if (result != null) {
            Logger.log("Result found for task %d", taskId);
        } else {
            Logger.log("No result found for task %d", taskId);
        }

        return Optional.ofNullable(result);
    }

    /**
     * Returns the number of stored story results.
     *
     * @return The current size of the storage
     */
    public int size() {
        int currentSize = storage.size();
        Logger.log("Storage size requested: %d results", currentSize);
        return currentSize;
    }

    /**
     * Returns an unmodifiable snapshot of all stored results.
     * The returned map is a copy and cannot be modified.
     *
     * @return An unmodifiable map of task IDs to story results
     */
    public Map<Long, StoryResult> snapshot() {
        Logger.log("Creating storage snapshot");

        // Create an unmodifiable copy
        Map<Long, StoryResult> snapshot = Collections.unmodifiableMap(new ConcurrentHashMap<>(storage));

        Logger.log("Snapshot created with %d results", snapshot.size());
        return snapshot;
    }

    /**
     * Clears all stored story results.
     */
    public void clear() {
        Logger.log("Clearing storage (%d results)", storage.size());
        storage.clear();
        Logger.log("Storage cleared");
    }
}
