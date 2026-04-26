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

        // Запуск первого пользовательского потока
        Thread userTask1 = new Thread(() -> {
            log("User task 1 starting");
            try {
                // Симуляция короткого вычисления (например, 2 секунды)
                Thread.sleep(2000);
                log("User task 1 completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log("User task 1 interrupted");
            }
        },"UserTask1");
        userTask1.start();

        // Запуск второго пользовательского потока (опционально, для 2 потоков)
        Thread userTask2 = new Thread(() -> {
            log("User task 2 starting");
            try {
                // Симуляция короткого вычисления (например, 1 секунда)
                Thread.sleep(1000);
                log("User task 2 completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log("User task 2 interrupted");
            }
        },"UserTask2");
        userTask2.start();

//         (Optional - just for demo )Ожидание завершения пользовательских потоков (чтобы main не завершился сразу)
        try {
            userTask1.join();
            userTask2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log("All user tasks completed, main thread exiting");


    }
}
