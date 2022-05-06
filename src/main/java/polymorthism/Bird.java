package polymorthism;

public class Bird extends Animal {

    @Override
    boolean isFlying() {
        return true;
    }

    @Override
    public int getLevel() {
        return 50;
    }
}
