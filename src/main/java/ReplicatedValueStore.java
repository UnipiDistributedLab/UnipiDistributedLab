
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.examples.LamportClock.*;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class ReplicatedValueStore {
    private static final Logger logger = Logger.getLogger(ValueStore.class.getName());
    private Server server;
    private ValueStoreGrpc.ValueStoreBlockingStub blockingStub = null;
    private LamportClock clock;
    private Lock lock = new ReentrantLock();
    private Map<Integer, String> storage = new TreeMap<>();

    private void start(int port) throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(new ReplicatedValueStore.ReplicatedValueStoreImpl())
                .build()
                .start();
        clock = new LamportClock(0);
        logger.info("Server started, listening on " + port);

        if (port == 50051) {
            blockingStub = ValueStoreGrpc.newBlockingStub(
                    ManagedChannelBuilder.forTarget("localhost:50052")
                            .usePlaintext()
                            .build());

            Runnable periodicWork = () -> {
                UpdateRequest request = null;
                try {
                    lock.lock();
                    request = UpdateRequest.newBuilder().putAllMap(storage).build();
                } finally {
                    lock.unlock();
                }
                blockingStub.updateSecondary(request);
            };

            ScheduledThreadPoolExecutor carrierThread = new ScheduledThreadPoolExecutor(1);
            carrierThread.scheduleAtFixedRate(periodicWork, 0, 2, TimeUnit.SECONDS);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    ReplicatedValueStore.this.stop();
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
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        Runnable initPrimary = () -> {
            final ReplicatedValueStore primaryServer = new ReplicatedValueStore();
            try {
                primaryServer.start(50051);
                primaryServer.blockUntilShutdown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        Runnable initSecondary = () -> {
            final ReplicatedValueStore secondaryServer = new ReplicatedValueStore();
            try {
                secondaryServer.start(50052);
                secondaryServer.blockUntilShutdown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        Thread primary = new Thread(initPrimary);
        Thread secondary = new Thread(initSecondary);

        primary.start();
        secondary.start();

        primary.join();
        secondary.join();
        System.out.println("All threads have successfully terminated");
    }

    class ReplicatedValueStoreImpl extends ValueStoreGrpc.ValueStoreImplBase {

        @Override
        public void write(WriteRequest req, StreamObserver<WriteReply> responseObserver) {
            clock.tick(req.getCounter());
            try {
                lock.unlock();
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
                lock.unlock();
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
    }
}