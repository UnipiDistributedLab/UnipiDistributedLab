package servers.lamportstorage;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class AtomicStorage {

    private Lock lock = new ReentrantLock(true);
    private Map<Integer, String> storage = new TreeMap<>();

    public void put(int key, String value) {
        try {
            lock.lock();
            storage.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    public void putAll(Map<Integer, String> mapData) {
        try {
            lock.lock();
            storage.clear();
            storage.putAll(mapData);
        } finally {
            lock.unlock();
        }
    }

    public Map<Integer, String> getAll() {
        try {
            lock.lock();
           return storage;
        } finally {
            lock.unlock();
        }
    }

    public String get(int key) {
        try {
            lock.lock();
            return storage.get(key);
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        try {
            lock.lock();
            return storage.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        try {
            lock.lock();
            return storage.toString();
        } finally {
            lock.unlock();
        }
    }
}
