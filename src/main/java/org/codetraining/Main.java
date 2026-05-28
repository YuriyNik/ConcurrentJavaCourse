package org.codetraining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {
//        System.out.printf("Hello and welcome!");
        // Get the number of available CPU cores
        int cpuCount = Runtime.getRuntime().availableProcessors();
        System.out.printf("\nNumber of available CPUs: %d%n", cpuCount);

        // 1. Массив строк -> List строк
        String[] strArr = {"A", "B"};
        List<String> strList = new ArrayList<>(Arrays.asList(strArr));
// 2. List строк -> Массив строк
        String[] outArr = strList.toArray(new String[0]);
// 3. Массив примитивов (int[]) -> List<Integer>
        int[] primitives = {1, 2, 3};
        List<Integer> boxedList = Arrays.stream(primitives).boxed().toList();

// 4. List<Integer> -> Массив примитивов (int[])
        int[] intArr = boxedList.stream().mapToInt(Integer::intValue).toArray();


//        // Basic test for single user
//        RateLimiter rl = new RateLimiter();
//        for (int i = 0; i < 120; i++) {
//            System.out.println("allowRequest " + i + "=" + rl.allowRequest("user_1"));
//        }

        // Concurrent load test for rate limiter with 100 tasks for different users
        runConcurrentLoadTest();
    }

    /**
     * Runs a concurrent load test with 100 tasks for different users using CompletableFuture.
     * Tests the RateLimiter under concurrent load with multiple users making requests.
     */
    private static void runConcurrentLoadTest() {
        System.out.println("\n=== CONCURRENT LOAD TEST START ===");

        RateLimiter rateLimiter = new RateLimiter();
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        // Create 100 tasks distributed across 5 different users
        List<String> users = List.of("user_A", "user_B", "user_C", "user_D", "user_E");
        int tasksPerUser = 20; // 20 tasks per user = 100 total

        // Track results
        AtomicInteger totalAllowed = new AtomicInteger(0);
        AtomicInteger totalDenied = new AtomicInteger(0);
        Map<String, AtomicInteger> userResults = new ConcurrentHashMap<>();

        // Initialize user result counters
        users.forEach(user -> userResults.put(user, new AtomicInteger(0)));

        long startTime = System.nanoTime();

        // Create and execute 100 concurrent tasks
        List<CompletableFuture<Void>> futures = IntStream.range(0, 650)
                .mapToObj(taskId -> {
                    String userId = users.get(taskId % users.size()); // Distribute tasks across users

                    return CompletableFuture.runAsync(() -> {
                        // Simulate some processing time
                        try {
                            Thread.sleep(1); // 1ms delay to simulate real work
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        // Make the rate-limited request
                        boolean allowed = rateLimiter.allowRequest(userId);

                        // Update counters
                        if (allowed) {
                            totalAllowed.incrementAndGet();
                            userResults.get(userId).incrementAndGet();
                        } else {
                            totalDenied.incrementAndGet();
                        }

                        // Log result (only for first few tasks to avoid spam)
                        if (taskId < 10) {
                            System.out.printf("Task %d (user: %s): %s%n",
                                    taskId, userId, allowed ? "ALLOWED" : "DENIED");
                        }

                    }, executor);
                })
                .toList();

        // Wait for all tasks to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        // Print results
        System.out.println("\n=== LOAD TEST RESULTS ===");
        System.out.printf("Total tasks: 100%n");
        System.out.printf("Duration: %d ms%n", durationMs);
        System.out.printf("Allowed requests: %d%n", totalAllowed.get());
        System.out.printf("Denied requests: %d%n", totalDenied.get());
        System.out.printf("Success rate: %.1f%%%n",
                (totalAllowed.get() * 100.0) / (totalAllowed.get() + totalDenied.get()));

        System.out.println("\nPer-user results:");
        userResults.forEach((user, count) ->
                System.out.printf("  %s: %d requests allowed%n", user, count.get()));

        // Cleanup
        executor.shutdown();
        executor.close();

        System.out.println("=== CONCURRENT LOAD TEST END ===");
    }
}