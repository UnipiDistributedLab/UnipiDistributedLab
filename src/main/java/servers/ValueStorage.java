package servers;

import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.examples.servers.LamportClock.*;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

enum StorageType {
    READ, WRITE
}
public class ValueStorage {

    private static final Logger logger = Logger.getLogger(ValueStorage.class.getName());
    private Server server;
    private ValueStoreGrpc.ValueStoreBlockingStub blockingStub;
    private LamportClock clock;
    private Lock lock = new ReentrantLock();
    private Map<Integer, String> storage = new TreeMap<>();
    private ScheduledThreadPoolExecutor carrierThread = new ScheduledThreadPoolExecutor(1);
    private Runnable periodicWork;
    private ScheduledFuture periodicScheduler;
    private Integer period = 10;
    private String otherServer;
    private StorageType type;

    public ValueStorage(StorageType type) {
        this.type = type;
        clock = new LamportClock(0);
    }

    public ValueStorage(StorageType type, String otherServer) {
        this.type = type;
        this.otherServer = otherServer;
        clock = new LamportClock(0);
        if (type == StorageType.READ) return;
        periodicSync(otherServer);
    }

    public void start(int port) throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(new ValueStoreImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    ValueStorage.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private void periodicSync(String otherServer) {
        if (otherServer != null) {
            blockingStub = ValueStoreGrpc.newBlockingStub(
                    ManagedChannelBuilder.forTarget(otherServer)
                            .usePlaintext()
                            .build());
            periodicWork = () -> {
                UpdateRequest request = null;
                try {
                    lock.lock();
                    request = UpdateRequest
                            .newBuilder()
                            .putAllMap(storage)
                            .build();
                } finally {
                    lock.unlock();
                    if (storage.isEmpty()) return;
                    try {
                        blockingStub.updateSecondary(request);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        periodicScheduler.notify();
                        carrierThread.scheduleAtFixedRate(periodicWork, 2, period, TimeUnit.SECONDS);
                    }
                }
            };
            carrierThread.scheduleAtFixedRate(periodicWork, 2, period, TimeUnit.SECONDS);
        }
    }

    class ValueStoreImpl extends ValueStoreGrpc.ValueStoreImplBase {

        @Override
        public void write(WriteRequest req, StreamObserver<WriteReply> responseObserver) {
            clock.tick(req.getCounter());
            try {
                lock.lock();
                storage.put(clock.getClock(), req.getValue() + ":" + req.getTimestamp());
            } finally {
                lock.unlock();
            }
            WriteReply reply = WriteReply.newBuilder()
                    .setValue(req.getValue())
                    .setCounter(clock.getClock())
                    .setTimestamp(req.getTimestamp())
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void readAll(Empty req, StreamObserver<ReadAllReply> responseObserver) {
            String storageOutput = null;
            try {
                lock.lock();
                storageOutput = storage.toString();
            } finally {
                lock.unlock();
            }
            ReadAllReply reply = ReadAllReply.newBuilder().setData(storageOutput).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void updateSecondary(UpdateRequest req, StreamObserver<UpdateReply> responseObserver) {
            Map<Integer, String> transmittedMap = req.getMapMap();
            try {
                lock.lock();
                storage.clear();
                storage.putAll(transmittedMap);
            } finally {
                lock.unlock();
            }
            UpdateReply reply = UpdateReply.newBuilder().setStatus(0).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void read(ReadRequest request, StreamObserver<ReadReply> responseObserver) {
            try {
                lock.lock();
                String storedValue = storage.get(request.getCounter());
                int syncAttemps = 0;
                //here we achieve waiting for synchronization
                while (storedValue == null) {
                    lock.unlock();
                    Thread.sleep(100);
                    lock.lock();
                    storedValue = storage.get(request.getCounter());
                    logger.info("Attemps no: " + syncAttemps);
                    syncAttemps += 1;
                }
                String[] splits = storedValue.split(":");
                String value = splits[0];
                String timeStamp = splits[1];
                ReadReply reply = ReadReply
                        .newBuilder()
                        .setValue(Integer.parseInt(value))
                        .setTimestamp(timeStamp)
                        .setCounter(clock.getClock())
                        .build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }
}
