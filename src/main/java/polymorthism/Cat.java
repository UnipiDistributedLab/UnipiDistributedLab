package polymorthism;

public class Cat extends Animal {

    @Override
    boolean isFlying() {
        return  false;
    }

    @Override
    public int getLevel() {
        return 70;
    }
}
