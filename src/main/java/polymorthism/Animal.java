package polymorthism;

interface Friendliness {
    int getLevel();
}

public abstract class Animal implements Friendliness {
    void sound() {}
    boolean isFlying() {
        return false;
    }
}