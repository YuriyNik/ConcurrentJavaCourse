package org.codetraining.part1;

import java.time.LocalTime;
import java.util.concurrent.ThreadLocalRandom;

public class StartVsRunDemo {

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
    static void randomSleep(){
        int ms = ThreadLocalRandom.current().nextInt(10,81);
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static class MyThread extends Thread{
    public MyThread(String name) {
        super(name);
    }

    @Override
    public void run() {
      for (int i = 0; i <= 5; i++) {
        log("MyThread step " +i);
        randomSleep();
      }
     }
    }
    static class MyRunnnable implements Runnable{
        @Override
        public void run() {
            for (int i = 0; i <= 5; i++) {
                log("Runnable step " +i);
                randomSleep();
            }
        }
    }


    public static void main(String[] args) throws InterruptedException {
       log("MAIN started");
       MyThread task1 = new MyThread("Worker-Thread");
       Thread task2 = new Thread(new MyRunnnable(),"Worker-Runnable");
       Thread task3 = new Thread(()->{
           for (int i = 0; i <= 5; i++) {
               log("Lambda step " +i);
               randomSleep();
           }
       },"Worker-Lambda");

        // ===============================
        // OPTION 1: run() -> same main thread
        // Uncomment to test

//        log("Calling run()");
//        task1.run();
//        task2.run();
//        task3.run();


        // ===============================
        // OPTION 2: start() -> new threads
        log("Before start(): task1=" + task1.getState());
        log("Before start(): task2=" + task2.getState());
        log("Before start(): task3=" + task3.getState());

        task1.start();
        task2.start();
        task3.start();

        log("After start(): task1=" + task1.getState());
        log("After start(): task2=" + task2.getState());
        log("After start(): task3=" + task3.getState());

        log("MAIN sleeping...");
        Thread.sleep(100);

        log("During sleep/after wakeup:");
        log("task1=" + task1.getState());
        log("task2=" + task2.getState());
        log("task3=" + task3.getState());

        task1.join();
        task2.join();
        task3.join();

        log("After join():");
        log("task1=" + task1.getState());
        log("task2=" + task2.getState());
        log("task3=" + task3.getState());

        log("MAIN finished");

    }
}
