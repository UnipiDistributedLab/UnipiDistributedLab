import io.grpc.unipi.election.ElectionResponse;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ClassB {

    private ClassBListener listener;
    private String stringTest;


    public ClassB(ClassBListener classA) {
        this.listener = classA;
    }

    public void fireJob() {
        Runnable runnable = () -> {
           //TODO add db connection
            String test = "Hello there";
            listener.response(test);
        };
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
        scheduler.schedule(runnable, 5, TimeUnit.SECONDS);
    }

    public interface ClassBListener {
        void response(String name);
    }
}
