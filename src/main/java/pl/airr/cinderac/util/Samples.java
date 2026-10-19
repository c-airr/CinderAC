package pl.airr.cinderac.util;

import java.util.LinkedList;

/** Bounded sample list: when full, the oldest is dropped. Not thread-safe. */
public final class Samples<T> extends LinkedList<T> {
    private final int capacity;

    public Samples(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public boolean add(T value) {
        if (size() >= capacity) removeFirst();
        return super.add(value);
    }

    public boolean isFull() {
        return size() >= capacity;
    }

    public int capacity() {
        return capacity;
    }
}
