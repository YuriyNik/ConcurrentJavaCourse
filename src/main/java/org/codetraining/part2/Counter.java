package org.codetraining.part2;

public interface Counter {
    void increment();
    void incrementBy(long delta);
    long getCount();
}
