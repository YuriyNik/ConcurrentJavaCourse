package org.codetraining.part3;

/**
 * Represents a story task with an ID, author, and title.
 * This is an immutable record with built-in validation.
 */
public record StoryTask(long id, String author, String title) {

    /**
     * Factory method to create a StoryTask with validation.
     *
     * @param id The unique identifier for the task (must be positive)
     * @param author The author of the story (must not be null or blank)
     * @param title The title of the story (must not be null or blank)
     * @return A new StoryTask instance
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public static StoryTask of(long id, String author, String title) {
        if (id <= 0 || author == null || author.isBlank() || title == null || title.isBlank()) {
            throw new IllegalArgumentException("Invalid task");
        }
        return new StoryTask(id, author, title);
    }
}
