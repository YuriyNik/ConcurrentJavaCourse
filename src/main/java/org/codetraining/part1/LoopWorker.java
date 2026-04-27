package org.codetraining.part1;

import java.time.LocalTime;

public class LoopWorker {
    private Thread thread;
    private String name;
    private int counter = 0;

    public LoopWorker(String name) {
        this.name = name;
    }

    public void start() {
        thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                counter++;
                log("counter = " + counter);
                try {
                    Thread.sleep(250); // 200-300 мс
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log("caught InterruptedException, exiting");
                    break;
                }
            }
            if (Thread.currentThread().isInterrupted()) {
                log("detected interrupt flag, exiting cleanly");
            }
            log("worker stopped, final counter = " + counter);
        }, name);
        thread.start();
    }

    public void stopAsync() {
        if (thread != null) {
            thread.interrupt();
            log("stopAsync() called");
        }
    }

    private void log(String message) {
        Thread tr = Thread.currentThread();
        System.out.printf(
                "%s | Thread=%s | %s%n",
                LocalTime.now(),
                tr.getName(),
                message
        );
    }

    public static void main(String[] args) {
        LoopWorker worker = new LoopWorker("worker-1");
        worker.start();

        // Даём поработать 1.5-2 сек
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Останавливаем
        System.out.println("\n--- Calling stopAsync() ---\n");
        worker.stopAsync();

        // Даём время на завершение
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\n--- Main thread exiting ---\n");
    }
}
