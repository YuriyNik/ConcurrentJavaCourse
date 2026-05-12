package org.codetraining.part3;

import org.codetraining.utils.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Demonstration of the story processing pipeline.
 * Shows concurrent processing of multiple story tasks using the service and storage components.
 * Compares Fixed Thread Pool vs Virtual Threads performance.
 */
public class StoryPipelineDemo {
    // Configuration constants
    private static final int POOL_SIZE = 100;
    private static final int IO_MILLIS = 100;        // I/O delay for fetch/finalize/save
    private static final int CPU_MILLIS = 1;         // CPU load for edit
    private static final double CHANCE_OF_ERROR = 0.2; // probability of error in editText (0 < chance ≤ 1)

    // Test scenarios
    private static final int[] TASK_COUNTS = {10,100,1000 };

    public static void main(String[] args) {
        Logger.log("=== StoryPipelineDemo START ===");

        // Initialize service (shared across runs)
        StoryService service = new StoryService(IO_MILLIS, CPU_MILLIS, IO_MILLIS, CHANCE_OF_ERROR);

        // Run tests for different task counts
        for (int n : TASK_COUNTS) {
            Logger.log("\n=== TESTING WITH %d TASKS ===", n);

            // Create tasks
            List<StoryTask> tasks = createSampleTasks(n);
            Logger.log("Created %d sample tasks", tasks.size());
            long start = System.currentTimeMillis();
            // Run #1: Fixed Thread Pool
            runTest("Fixed Thread Pool", tasks, service, () -> Executors.newFixedThreadPool(POOL_SIZE));
            long endFixed = System.currentTimeMillis();
            // Run #2: Virtual Threads
            runTest("Virtual Threads", tasks, service, Executors::newVirtualThreadPerTaskExecutor);
            long endVirtual = System.currentTimeMillis();
            Logger.log("Comparison for %d tasks: Fixed Thread Pool completed in %s ms vs Virtual Threads completed in %s ms", n,endFixed-start,endVirtual-endFixed );
        }

        Logger.log("=== StoryPipelineDemo END ===");
    }

    /**
     * Runs a single test with the specified executor factory.
     */
    private static void runTest(String testName, List<StoryTask> tasks, StoryService service,
                               java.util.function.Supplier<ExecutorService> executorFactory) {
        Logger.log("--- %s TEST ---", testName);

        // Create fresh storage for this test
        StoryStorage storage = new StoryStorage();

        // Create executor
        try (ExecutorService executor = executorFactory.get()) {
            // Measure execution time
            long startTime = System.nanoTime();

            // Run the pipeline
            runPipeline(executor, tasks, service, storage);

            long endTime = System.nanoTime();
            long elapsedMs = (endTime - startTime) / 1_000_000;

            // Report results
            Logger.log("%s completed in %d ms", testName, elapsedMs);
            Logger.log("Results stored: %d (expected: %d, errors: %d)",
                    storage.size(), tasks.size(), tasks.size() - storage.size());

            // Show sample results
            displaySampleResults(storage, 2);
        }
    }

    /**
     * Creates sample story tasks for demonstration.
     */
    private static List<StoryTask> createSampleTasks(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> StoryTask.of(i,
                        "Author-" + i,
                        "Chapter-" + i))
                .toList();
    }

    /**
     * Runs the story processing pipeline using the provided executor.
     * Each task goes through: fetchDraft -> editText -> finalizeStory -> save
     * All steps are asynchronous with proper error handling.
     */
    private static void runPipeline(ExecutorService executor, List<StoryTask> tasks,
                                   StoryService service, StoryStorage storage) {

        // Create asynchronous pipeline for each task
        List<CompletableFuture<Void>> futures = tasks.stream()
                .map(task -> {
                    // Step 1: Fetch draft (I/O, on provided executor)
                    CompletableFuture<String> fetchFuture = CompletableFuture
                            .supplyAsync(() -> service.fetchDraft(task), executor);

                    // Step 2: Edit text (CPU, on common ForkJoinPool for demonstration)
                    CompletableFuture<String> editFuture = fetchFuture
                            .thenApplyAsync(service::editText); // Uses common pool

                    // Step 3: Finalize story (I/O, on provided executor)
                    CompletableFuture<StoryResult> finalizeFuture = editFuture
                            .thenApplyAsync(editedText -> service.finalizeStory(task, editedText), executor);

                    // Step 4: Save result (I/O, on provided executor)
                    CompletableFuture<Void> saveFuture = finalizeFuture
                            .thenAcceptAsync(storage::save, executor);

                    // Error handling: log errors but don't crash the pipeline
                    return saveFuture.exceptionally(throwable -> {
                        Logger.error("Pipeline failed for task %d: %s", task.id(), throwable.getMessage());
                        return null; // Don't save anything on error
                    });
                })
                .toList();

        Logger.log("Wait for all pipelines to complete for %s tasks", tasks.size());
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        Logger.log("All pipelines completed for %s tasks", tasks.size());
    }

    /**
     * Displays sample results from storage.
     */
    private static void displaySampleResults(StoryStorage storage, int maxSamples) {
        var snapshot = storage.snapshot();

        if (snapshot.isEmpty()) {
            Logger.log("No results to display");
            return;
        }

        Logger.log("Sample results (showing up to %d):", maxSamples);
        snapshot.entrySet().stream()
                .limit(maxSamples)
                .forEach(entry -> {
                    String preview = entry.getValue().finalText();
                    if (preview.length() > 80) {
                        preview = preview.substring(0, 80) + "...";
                    }
                    Logger.log("Task %d: %s", entry.getKey(), preview);
                });

        if (snapshot.size() > maxSamples) {
            Logger.log("... and %d more results", snapshot.size() - maxSamples);
        }
    }
}
