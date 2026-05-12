package org.codetraining.part3;

/**
 * Represents the result of processing a story task.
 * Contains the task ID and the final generated text.
 */
public record StoryResult(long taskId, String finalText) {

    /**
     * Factory method to create a StoryResult with validation.
     *
     * @param taskId The ID of the task this result belongs to (must be positive)
     * @param text The final generated text (must not be null or blank)
     * @return A new StoryResult instance
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public static StoryResult of(long taskId, String text) {
        if (taskId <= 0 || text == null || text.isBlank()) {
            throw new IllegalArgumentException("Invalid result");
        }
        return new StoryResult(taskId, text);
    }
}
