
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.examples.LamportClock.*;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ValueStore {
    private static final Logger logger = Logger.getLogger(ValueStore.class.getName());
    private Server server;
    private LamportClock clock;
    private Map<Integer, String> storage = new TreeMap<>();

    private void start(int port) throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(new ValueStore.ValueStoreImpl())
                .build()
                .start();
        clock = new LamportClock(0);
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    ValueStore.this.stop();
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
        final ValueStore primaryServer = new ValueStore();
        final ValueStore secondaryServer = new ValueStore();
        primaryServer.start(50051);
        secondaryServer.start(50052);
        primaryServer.blockUntilShutdown();
        secondaryServer.blockUntilShutdown();
    }

    class ValueStoreImpl extends ValueStoreGrpc.ValueStoreImplBase {

        @Override
        public void write(WriteRequest req, StreamObserver<WriteReply> responseObserver) {
            clock.tick(req.getCounter());
            storage.put(clock.getClock(), req.getValue() + ":" + req.getTimestamp());
            WriteReply reply = WriteReply.newBuilder()
                    .setValue(req.getValue())
                    .setCounter(clock.getClock())
                    .setTimestamp(req.getTimestamp())
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        public void readAll(Empty req, StreamObserver<ReadAllReply> responseObserver) {
            ReadAllReply reply = ReadAllReply.newBuilder().setData(storage.toString()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}