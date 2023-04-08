package rpc;

import io.grpc.stub.StreamObserver;
import io.grpc.unipi.distributed.StreamMessage;
import io.grpc.unipi.distributed.StreamTestGrpc;
import io.grpc.unipi.distributed.UserId;
import io.grpc.unipi.distributed.UserModel;

import java.util.LinkedHashSet;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class StreamTestImpl extends StreamTestGrpc.StreamTestImplBase {

    private final LinkedHashSet<StreamObserver<StreamMessage>> observers = new LinkedHashSet<>();
    private int counter = 0;

    public StreamTestImpl() {
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
        Runnable runnable = () -> {
            counter++;
            for (StreamObserver<StreamMessage> observer : observers) {
                StreamMessage message = StreamMessage.newBuilder().setNewValue(counter).build();
                observer.onNext(message);
            }
        };
        scheduler.scheduleAtFixedRate(runnable, 0, 3, TimeUnit.SECONDS);
    }

    @Override
    public void listenNewValue(UserId request, StreamObserver<StreamMessage> responseObserver) {
        observers.add(responseObserver);
        StreamMessage message = StreamMessage.newBuilder().setNewValue(counter).build();
        responseObserver.onNext(message);
//        super.listenNewValue(request, responseObserver);
    }
}
