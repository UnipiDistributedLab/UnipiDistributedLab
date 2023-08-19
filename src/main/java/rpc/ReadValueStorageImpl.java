package rpc;

import io.grpc.examples.servers.LamportClock.*;
import io.grpc.stub.StreamObserver;
import storage.InMemoryStorage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ReadValueStorageImpl extends ReadValueStorageGrpc.ReadValueStorageImplBase {

    private InMemoryStorage writeStorage = new InMemoryStorage();
    private final Logger logger = Logger.getLogger(ReadValueStorageImpl.class.getName());

    @Override
    public void doRead(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {

        String value = writeStorage.get(request.getTimeStamp());
        try {
            int counter = 0;
            while (value == null) {
                Thread.sleep(100);
                value = writeStorage.get(request.getTimeStamp());
                counter ++;
                logger.info("Attempt # "+ counter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ReadResponse response = ReadResponse
                .newBuilder()
                .setValue(value)
                .setResponseMessage("OK")
                .setMessageCode(200).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void doUpdate(UpdateRequest request, StreamObserver<UpdateResponse> responseObserver) {
        writeStorage.replaceAll(request.getMapMap());
        UpdateResponse response = UpdateResponse
                .newBuilder()
                .setResponseMessage("OK")
                .setMessageCode(200).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
