package utlis;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Atomic<T> {

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private T cached;

    public void set(T value) {
        try {
            lock.writeLock().lock();
            cached = value;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void setNull() {
        try {
            lock.writeLock().lock();
            cached = null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public T get() {
        try {
            lock.readLock().lock();
            return cached;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isEmpty() {
        try {
            lock.readLock().lock();
            return cached == null;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public String toString() {
        try {
            lock.readLock().lock();
            return cached.toString();
        } finally {
            lock.readLock().unlock();
        }
    }
}
