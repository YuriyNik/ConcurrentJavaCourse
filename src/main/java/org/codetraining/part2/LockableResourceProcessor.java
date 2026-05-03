package org.codetraining.part2;

public interface LockableResourceProcessor {
    /**
     * Executes a critical section over two resources r1 and r2.
     * The selected strategy for resource acquisition is implemented inside (depends on implementation).
     */
    void processResources(LockableResource r1, LockableResource r2) throws InterruptedException;
}
