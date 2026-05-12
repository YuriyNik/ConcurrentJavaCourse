package org.codetraining.part3;

import org.codetraining.utils.BusyCpuUtil;
import org.codetraining.utils.Logger;
import org.codetraining.utils.ThreadSleepUtil;

import java.security.SecureRandom;

/**
 * Service for processing story tasks through multiple stages.
 * Simulates realistic processing with I/O delays, CPU work, and error scenarios.
 */
public final class StoryService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final int fetchDraftMs;
    private final int editTextMs;
    private final int finalizeMs;
    private final double chanceOfEditingError;

    /**
     * Creates a new StoryService with specified processing parameters.
     *
     * @param fetchDraftMs Time to simulate draft fetching (I/O delay) in milliseconds
     * @param editTextMs Time to simulate text editing (CPU work) in milliseconds
     * @param finalizeMs Time to simulate story finalization (I/O delay) in milliseconds
     * @param chanceOfEditingError Probability of editing error (0 < value ≤ 1)
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public StoryService(int fetchDraftMs, int editTextMs, int finalizeMs, double chanceOfEditingError) {
        if (fetchDraftMs < 0 || editTextMs < 0 || finalizeMs < 0) {
            throw new IllegalArgumentException("All timing parameters must be non-negative");
        }
        if (chanceOfEditingError <= 0 || chanceOfEditingError > 1) {
            throw new IllegalArgumentException("chanceOfEditingError must be in range (0, 1]");
        }

        this.fetchDraftMs = fetchDraftMs;
        this.editTextMs = editTextMs;
        this.finalizeMs = finalizeMs;
        this.chanceOfEditingError = chanceOfEditingError;

        Logger.log("StoryService initialized: fetch=%dms, edit=%dms, finalize=%dms, errorChance=%.2f",
                fetchDraftMs, editTextMs, finalizeMs, chanceOfEditingError);
    }

    /**
     * Simulates fetching a story draft from external storage.
     * Uses blocking sleep to simulate I/O delay.
     *
     * @param task The story task to fetch draft for
     * @return A draft string based on the task
     */
    public String fetchDraft(StoryTask task) {
        Logger.log("Fetching draft for task %d by %s: '%s'",
                task.id(), task.author(), task.title());

        // Simulate I/O delay
        ThreadSleepUtil.safeSleepWithoutThrow(fetchDraftMs);

        // Generate a simple draft based on the task
        String draft = String.format("Draft for '%s' by %s (ID: %d)",
                task.title(), task.author(), task.id());

        Logger.log("Draft fetched: %s", draft);
        return draft;
    }

    /**
     * Simulates editing text content.
     * Uses CPU busy-loop and has a chance of throwing an exception to simulate editing errors.
     *
     * @param draft The draft text to edit
     * @return The edited text
     * @throws RuntimeException if editing fails (based on error probability)
     */
    public String editText(String draft) {
        Logger.log("Editing text: %s", draft);

        // Simulate CPU work
        BusyCpuUtil.spinOnCpuMillis(editTextMs);

        // Check for editing error based on probability
        if (RANDOM.nextDouble() < chanceOfEditingError) {
            Logger.log("Editing error occurred during processing");
            throw new RuntimeException("Failed to edit text: editing error");
        }

        // Simulate some editing by adding a suffix
        String edited = draft + " [EDITED]";
        Logger.log("Text edited successfully: %s", edited);
        return edited;
    }

    /**
     * Simulates finalizing a story and creating the result.
     * Combines I/O simulation with result creation.
     *
     * @param task The original story task
     * @param editedText The edited text content
     * @return A StoryResult containing the final story
     */
    public StoryResult finalizeStory(StoryTask task, String editedText) {
        Logger.log("Finalizing story for task %d", task.id());

        // Simulate finalization I/O delay
        ThreadSleepUtil.safeSleepWithoutThrow(finalizeMs);

        // Create final text by combining task info with edited content
        String finalText = String.format("FINAL STORY: %s%nAuthor: %s%n%s",
                task.title(), task.author(), editedText);

        StoryResult result = StoryResult.of(task.id(), finalText);
        Logger.log("Story finalized: %s", result);
        return result;
    }
}
