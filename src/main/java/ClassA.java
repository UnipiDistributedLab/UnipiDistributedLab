
public class ClassA implements ClassB.ClassBListener {


    public static void main(String[] args) {
        ClassA test = new ClassA();
        test.startTest();
    }

    private void startTest() {
        ClassB classB = new ClassB(this);
        classB.fireJob();
    }

    @Override
    public void response(String name) {
        String iReceived = name;
    }
}
