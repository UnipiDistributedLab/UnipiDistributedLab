package servers;

import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.servers.LamportClock.*;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import static spark.Spark.get;
import static spark.Spark.post;

public class ReplicatedValueStore {
    private static ValueStoreGrpc.ValueStoreBlockingStub writeBlockingStub;
    private static ValueStoreGrpc.ValueStoreBlockingStub readBlockingStub;
    private static final Logger logger = Logger.getLogger(ReplicatedValueStore.class.getName());

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        post("/save", (req, res) -> {
            return "Hello world";
        });
        get("/read", (req, res) -> {
            return "Hello world";
        });

        Runnable initWriteHost = () -> {
            final ValueStorage primaryServer = new ValueStorage(StorageType.WRITE, "localhost:50052");
            try {
                primaryServer.start(50051);
                primaryServer.blockUntilShutdown();
                writeBlockingStub = ValueStoreGrpc.newBlockingStub(
                        ManagedChannelBuilder.forTarget("localhost:50051")
                                .usePlaintext()
                                .build());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        Runnable initReadHost = () -> {
            final ValueStorage secondaryServer = new ValueStorage(StorageType.READ);
            try {
                secondaryServer.start(50052);
                secondaryServer.blockUntilShutdown();
                writeBlockingStub = ValueStoreGrpc.newBlockingStub(
                        ManagedChannelBuilder.forTarget("localhost:50052")
                                .usePlaintext()
                                .build());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        Thread writeThread = new Thread(initWriteHost);
        Thread readThread = new Thread(initReadHost);

        readThread.start();
        writeThread.start();

        readThread.join();
        writeThread.join();
        System.out.println("All threads have successfully terminated");
    }

    public int write(int value, int lamportTimestamp) {
        logger.info("Sending " + value + "to server" + writeBlockingStub.getChannel().toString() + "\n");
        String realtime = new Timestamp(System.currentTimeMillis()).toString();

        WriteRequest request = WriteRequest.newBuilder()
                .setValue(value)
                .setCounter(lamportTimestamp)
                .setTimestamp(realtime)
                .build();
        WriteReply response;
        try {
            response = writeBlockingStub.write(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            //TODO think twice because resets get clock
            return 0;
        }
        logger.info("Received: " + response.getCounter() + ":" + response.getTimestamp() + ":" + response.getValue());
        return response.getCounter();
    }
}