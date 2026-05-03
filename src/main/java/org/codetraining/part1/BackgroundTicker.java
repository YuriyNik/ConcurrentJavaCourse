package org.codetraining.part1;

import java.time.LocalTime;

public class BackgroundTicker {
    static void log(String message) {
        Thread tr = Thread.currentThread();
        System.out.printf(
                "%s | Thread=%s | State=%s | %s%n",
                LocalTime.now(),
                tr.getName(),
                tr.getState(),
                message
        );
    }
    static Thread startDaemon(String name, int periodMillis) {
        Thread task = new Thread(()->{
            while (!Thread.currentThread().isInterrupted()) {
            log("tick");
                try {
                    Thread.sleep(periodMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        task.setName(name);
        task.setDaemon(true);
        task.start();
        return task;
    }

    public static void main(String[] args) {
        startDaemon("ticker", 200);

        // Start first user thread
        Thread userTask1 = new Thread(() -> {
            log("User task 1 starting");
            try {
                // Simulate short computation (e.g., 2 seconds)
                Thread.sleep(2000);
                log("User task 1 completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log("User task 1 interrupted");
            }
        },"UserTask1");
        userTask1.start();

        // Start second user thread (optional, for 2 threads)
        Thread userTask2 = new Thread(() -> {
            log("User task 2 starting");
            try {
                // Simulate short computation (e.g., 1 second)
                Thread.sleep(1000);
                log("User task 2 completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log("User task 2 interrupted");
            }
        },"UserTask2");
        userTask2.start();

        // (Optional - just for demo) Wait for user threads to complete (so main doesn't exit immediately)
        try {
            userTask1.join();
            userTask2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log("All user tasks completed, main thread exiting");


    }
}
