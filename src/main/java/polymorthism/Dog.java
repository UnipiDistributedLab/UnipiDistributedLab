package polymorthism;

public class Dog extends Animal {
    @Override
    boolean isFlying() {
        return  false;
    }

    @Override
    public int getLevel() {
        return 100;
    }
}