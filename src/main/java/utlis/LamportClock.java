package utlis;

import java.util.concurrent.locks.ReentrantLock;

public class LamportClock {
    private volatile int counter;
    private ReentrantLock lock = new ReentrantLock(true);
//    private Lock readLock = reentrantReadWriteLock.readLock();
//    private Lock writeLock = reentrantReadWriteLock.writeLock();

    public LamportClock(int counter) {
        this.counter = counter;
    }

    public int tick(int requestTime) {
        try {
            lock.lock();
            setClock(requestTime);
            return increase();
        } catch (Exception e) {
            lock.unlock();
            System.out.println(e.getMessage());
            return 0;
        } finally {
            lock.unlock();
        }
    }

    public synchronized void setClock(int requestTime) {
        counter = Integer.max(counter, requestTime);
    }

    public synchronized int increase() {
        counter ++;
        return counter;
    }

    public int getClock() {
        try {
            lock.lock();
            return counter;
        } catch (Exception e) {
            lock.unlock();;
            return counter;
        } finally {
            lock.unlock();
        }
    }

}
