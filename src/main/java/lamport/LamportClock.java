package lamport;

public final class LamportClock {
    private int counter;

    public void compareCounter(int counter) {
        if (this.counter < counter) {
            this.counter = counter;
        }
    }

    public void increase() {
        counter ++;
    }

    public int getCounter() {
        return counter;
    }
}
