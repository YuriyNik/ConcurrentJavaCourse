package org.codetraining.part1;

import java.util.ArrayList;
import java.util.List;

public class MutexCounterDemo {

    static void runRace(int threads, int itersPerThread,Counter counter) throws InterruptedException {
        List<Thread> tasks = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
        tasks.add(new Thread(()->{
            for (int j = 0; j < itersPerThread; j++) {
                counter.inc();
            }
        },"worker-"+i));
        }
        for (Thread task:tasks){
            task.start();
            System.out.println("Started "+task.getName());
        }
        for (Thread task:tasks){
            System.out.println("waiting for .. "+task.getName());
            task.join();
        }
        System.out.println("Result expected="+threads*itersPerThread+";actual="+counter.value());
    }

    public static void main(String[] args) throws InterruptedException {
        runRace(8, 100_000,new UnsafeCounter());
        runRace(8, 100_000,new SynchronizedCounter());
    }

}
class UnsafeCounter implements Counter{
    private long count=0;
    public void inc(){
    count++;
    }
    public long value(){
    return count;
    }
}
class SynchronizedCounter implements Counter{
    private long count=0;
    public void inc(){
    synchronized (this){
        count++;
    }
    }
    public synchronized long value(){
    return count;
    }
}
interface Counter{
    void inc();
    long value();
}
