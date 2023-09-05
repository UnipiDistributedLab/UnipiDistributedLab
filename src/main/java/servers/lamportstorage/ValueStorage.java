package servers.lamportstorage;

import io.grpc.ServerBuilder;
import io.grpc.examples.utlis.LamportClock.*;
import io.grpc.stub.StreamObserver;
import servers.leaderelection.ServerData;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

public final class ValueStorage {

    private static final Logger logger = Logger.getLogger(ValueStorage.class.getName());
    private ServerBuilder server;
    private AtomicStorage atomicStorage = new AtomicStorage();
    private ScheduledFuture periodicScheduler;
    private String otherServer;
    private StorageType type;
    private ServerData serverData;
    @Nullable
    private LeaderStorageHelper leaderStorageHelper;

    public ValueStorage(StorageType type, ServerData serverData) {
        this.type = type;
        this.serverData = serverData;
        if (type == StorageType.READ) return;
    }

    public ServerBuilder addValueService(ServerBuilder builder) {
        server = builder.addService(new ValueStoreImpl());
//        logger.info("Server started, listening on " + serverData.getUrl());
        return server;
    }

    public StorageType getType() {
        return type;
    }

    public void setLeader(LeaderStorageHelper leaderStorageHelper) {
        this.leaderStorageHelper = leaderStorageHelper;
    }

    public void stopOperations() {
        if (leaderStorageHelper == null) return;
        this.leaderStorageHelper.stopOperations();
        this.leaderStorageHelper = null;
    }

    class ValueStoreImpl extends ValueStoreGrpc.ValueStoreImplBase {

        @Override
        public void write(WriteRequest req, StreamObserver<WriteReply> responseObserver) {
        if (leaderStorageHelper != null) {
            leaderStorageHelper.write(req, responseObserver, req.getCounter());
            return;
        }
            atomicStorage.put(req.getCounter(), req.getValue() + ":" + req.getTimestamp());
            WriteReply reply = WriteReply.newBuilder()
                    .setValue(req.getValue())
                    .setCounter(req.getCounter())
                    .setTimestamp(req.getTimestamp())
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void readAll(Empty req, StreamObserver<ReadAllReply> responseObserver) {
            if (leaderStorageHelper != null) {
                leaderStorageHelper.readAll(req, responseObserver);
                return;
            }
            ReadAllReply reply = ReadAllReply
                    .newBuilder()
                    .putAllMap(atomicStorage.getAll())
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void updateSecondary(UpdateRequest req, StreamObserver<UpdateReply> responseObserver) {
            Map<Integer, String> transmittedMap = req.getMapMap();
            atomicStorage.putAll(transmittedMap);
            UpdateReply reply = UpdateReply.newBuilder().setStatus(0).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void read(ReadRequest request, StreamObserver<ReadReply> responseObserver) {
            if (leaderStorageHelper != null) {
                leaderStorageHelper.read(request, responseObserver);
                return;
            }
            try {
                String storedValue = atomicStorage.get(request.getCounter());
                int syncAttemps = 0;
                //here we achieve waiting for synchronization
                while (storedValue == null) {
                    Thread.sleep(100);
                    storedValue = atomicStorage.get(request.getCounter());
                    logger.info("Attemps no: " + syncAttemps);
                    syncAttemps += 1;
                }
                String[] splits = storedValue.split(":");
                String value = splits[0];
                String timeStamp = splits[1];
                ReadReply reply = ReadReply
                        .newBuilder()
                        .setValue(value)
                        .setTimestamp(timeStamp)
                        .setCounter(request.getCounter())
                        .build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
