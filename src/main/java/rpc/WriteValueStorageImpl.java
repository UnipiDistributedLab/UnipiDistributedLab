package rpc;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.servers.LamportClock.*;
import io.grpc.stub.StreamObserver;
import io.grpc.unipi.distributed.StreamMessage;
import storage.InMemoryStorage;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WriteValueStorageImpl extends WriteValueStorageGrpc.WriteValueStorageImplBase {

    private ReadValueStorageGrpc.ReadValueStorageBlockingStub readValueStorageStub;
    private InMemoryStorage writeStorage = new InMemoryStorage();
    private final int syncPeriod = 15;

    public WriteValueStorageImpl() {
        Channel readChannel = ManagedChannelBuilder
                .forAddress("localhost", 2434)
                .usePlaintext()
                .build();
        readValueStorageStub = ReadValueStorageGrpc.newBlockingStub(readChannel);
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
        Runnable runnable = () -> {
            UpdateRequest syncRequest = UpdateRequest
                    .newBuilder()
                    .putAllMap(writeStorage.getStorage())
                    .build();
            UpdateResponse response =  readValueStorageStub.doUpdate(syncRequest);
            //TODO discussion
        };
        scheduler.scheduleAtFixedRate(runnable, 0, syncPeriod, TimeUnit.SECONDS);
    }

    @Override
    public void doWrite(WriteRequest request, StreamObserver<WriteResponse> responseObserver) {
        int timestamp = writeStorage.save(request.getValue());
        WriteResponse response = WriteResponse
                .newBuilder()
                .setTimeStamp(timestamp)
                .setResponseMessage("OK")
                .setMessageCode(200).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
