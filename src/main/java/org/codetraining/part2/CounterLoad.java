package org.codetraining.part2;

import org.codetraining.utils.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

public class CounterLoad {
    public static void runLoad(Counter counter, int threadsCount, int iterationsPerThread) {
        // 1) Create threadsCount threads
        // 2) Synchronize start (CountDownLatch/CyclicBarrier)
        // 3) Each thread performs iterationsPerThread increments
        // 4) Measure time via System.nanoTime()
        // 5) Wait for all threads to complete
        // 6) Calculate expected = threadsCount * iterationsPerThread
        // 7) Print in one line:
        // "<Impl-X>: expected=<...>, actual=<...>, timeMs=<...>"

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadsCount);
        for (int i = 0; i < threadsCount; i++) {
            new Thread(()->
            {
                try {
                    startLatch.await();
                for (int j = 0; j < iterationsPerThread; j++) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    counter.increment();
                }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                finally {
                    finishLatch.countDown();
                }
            } , "worker-"+i
            ).start();
        }
        Logger.log("Starting load test: %d threads, %d iterations each", threadsCount, iterationsPerThread);
        long startTime = System.nanoTime();
        startLatch.countDown();

        try {
            finishLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long endTime = System.nanoTime();
        long timeMs = (endTime-startTime)/1_000_000;

        long expected = (long) threadsCount*iterationsPerThread;
        long actual = counter.getCount();

        Logger.log("%s: expected=%d, actual=%d, timeMs=%d",
                counter.getClass().getSimpleName(), expected, actual, timeMs);
    }

    public static void main(String[] args) {
        Counter counter = new UnsafeCounter();
//        CounterLoad.runLoad(counter, 8 , 2_500);
        CounterLoad.runLoad(counter, 32 , 1_000_000);
        Counter safeCounter = new SynchronizedCounter();
//        CounterLoad.runLoad(safeCounter, 8 , 2_500);
        CounterLoad.runLoad(safeCounter, 32 , 1_000_000);
        Counter atomicCounter = new AtomicCounter();
//        CounterLoad.runLoad(atomicCounter, 8 , 2_500);
        CounterLoad.runLoad(atomicCounter, 32 , 1_000_000);
        Counter reentrantLockCounter = new ReentrantLockCounter();
//        CounterLoad.runLoad(reentrantLockCounter, 8 , 2_500);
        CounterLoad.runLoad(reentrantLockCounter, 32 , 1_000_000);
        Counter adderCounter = new LongAdderCounter();
        //        CounterLoad.runLoad(adderCounter, 8 , 2_500);
        CounterLoad.runLoad(adderCounter, 32 , 1_000_000);
    }
}

class UnsafeCounter implements Counter {
    private long counter=0;
    @Override
    public void increment() {
        incrementBy(1);
    }

    @Override
    public void incrementBy(long delta) {
        counter = counter + delta;
    }

    @Override
    public long getCount() {
        return counter;
    }
}

class SynchronizedCounter implements Counter{
    private long counter=0;

    @Override
    public void increment() {
        incrementBy(1);
    }

    @Override
    public synchronized void incrementBy(long delta) {
        counter = counter + delta;
    }

    @Override
    public long getCount() {
        return counter;
    }
}

class AtomicCounter implements Counter{
    private final AtomicLong counter= new AtomicLong(0);
    @Override
    public void increment() {
        counter.incrementAndGet();
    }

    @Override
    public void incrementBy(long delta) {
        counter.addAndGet(delta);
    }

    @Override
    public long getCount() {
        return counter.get();
    }
}

class ReentrantLockCounter implements Counter{
    private long counter=0;
    private ReentrantLock lock = new ReentrantLock();
    @Override
    public void increment() {
        incrementBy(1);
    }

    @Override
    public void incrementBy(long delta) {
        try {
            lock.lock();
            counter = counter + delta;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public long getCount() {
        return counter;
    }
}

class LongAdderCounter implements Counter{
    private final LongAdder counter = new LongAdder();
    @Override
    public void increment() {
        counter.increment();
    }

    @Override
    public void incrementBy(long delta) {
        counter.add(delta);
    }

    @Override
    public long getCount() {
        return counter.longValue();
    }
}