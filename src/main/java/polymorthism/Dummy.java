package polymorthism;


import java.util.ArrayList;

public class Dummy {

    public static void main(String[] args) {
        ArrayList<Animal> array = new ArrayList<>();

        Dog dog = new Dog();
        array.add(dog);

        Cat cat = new Cat();
        array.add(cat);

        Bird bird = new Bird();
        array.add(bird);

        for (Animal animal: array) {
            animal.isFlying();
            animal.sound();
            animal.getLevel();
        }
    }

}
