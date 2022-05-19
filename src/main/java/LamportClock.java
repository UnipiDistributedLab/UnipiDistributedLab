public class LamportClock {
    private int counter;

    public LamportClock(int counter) {
        this.counter = counter;
    }

    public void tick(int requestTime) {
        counter = Integer.max(counter, requestTime);
        counter++;
    }

    public void increase() {
        counter++;
    }

    public int getClock() {
        return counter;
    }

}
