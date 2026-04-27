package org.codetraining.part1;

public class VolatileFlagDemo {

    private volatile boolean running;

    private Thread task;

    VolatileFlagDemo (){
    }

    public void start() {
        if (task != null && task.isAlive()) {
            return;
        }
        running = true;
        task = new Thread(()->{
            long counter = 0;
            while (running) {
                counter++;
            }
            System.out.println("Stopped. counter="+counter);
        },"worker");
        task.start();
    }
    public void stop() {
        running=false;
    }

    public void waitForStop(long ms) throws InterruptedException {
        if (task!=null) {
            task.join(ms);
        }
    }
    public boolean isAlive() {
        return task != null && task.isAlive();
    }

    public static void main(String[] args) throws InterruptedException {
        VolatileFlagDemo volatileFlagDemo = new VolatileFlagDemo();
        System.out.println("Starting task..");
        volatileFlagDemo.start();
        Thread.sleep(3000);
        System.out.println("Stopping the task");
        volatileFlagDemo.stop();
       volatileFlagDemo.waitForStop(3000);
       if (volatileFlagDemo.isAlive()) {
           System.out.println("Task didnt stop");
       }
       else {
           System.out.println("Task stops normally");
       }
        System.out.println("Finished");
    }
}
