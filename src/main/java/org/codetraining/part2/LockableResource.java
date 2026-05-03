package org.codetraining.part2;

import java.util.concurrent.locks.ReentrantLock;

public class LockableResource {
    private final String name;          // For example: "Name-A" / "Name-B"
    private final int id;               // For global order (fix #1)
    private final ReentrantLock lock = new ReentrantLock();
    private final Object monitor = new Object(); // For synchronized blocks

    LockableResource(String name, int id) {
        this.name = name;
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public int getId() {
        return id;
    }
    public Object getMonitor() {
        return monitor;
    }
    public ReentrantLock getLock() {
        return lock;
    }
}
