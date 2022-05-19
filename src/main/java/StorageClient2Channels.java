/*
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.LamportClock.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

*/
/**
 * A simple client that requests a greeting from the {@link ValueStore}.
 *//*

public class StorageClient2Channels {
    private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());

    private final ValueStoreGrpc.ValueStoreBlockingStub blockingStubPrimary;
    private final ValueStoreGrpc.ValueStoreBlockingStub blockingStubSecondary;
    private LamportClock clock = new LamportClock(-1);
    private Random generator = new Random();


    */
/**
     * Construct client for accessing HelloWorld server using the existing channel.
     *//*

    public StorageClient2Channels(ArrayList<Channel> channel) {
        blockingStubPrimary = ValueStoreGrpc.newBlockingStub(channel.get(0));
        blockingStubSecondary = ValueStoreGrpc.newBlockingStub(channel.get(1));
    }

    */
/**
     * Say hello to server.
     *//*

    public void write(int value, ValueStoreGrpc.ValueStoreBlockingStub blockingStub) {
        logger.info("Sending " + value + "to server" + blockingStub.getChannel().toString() + "\n");
        String realtime = new Timestamp(System.currentTimeMillis()).toString();
        clock.increase();
        int lamportTimestamp = clock.getClock();

        WriteRequest request = WriteRequest.newBuilder()
                .setValue(value)
                .setCounter(lamportTimestamp)
                .setTimestamp(realtime)
                .build();
        WriteReply response;
        try {
            response = blockingStub.write(request);
            clock.tick(request.getCounter());
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Received: " + response.getCounter() + ":" + response.getTimestamp() + ":" + response.getValue());
    }

    public static void main(String[] args) throws Exception {
        ArrayList<String> targets = new ArrayList<>();
        targets.add("localhost:50051");
        targets.add("localhost:50052");

        if (args.length > 0) {
            if ("--help".equals(args[0])) {
                System.err.println("Usage: [name [target]]");
                System.err.println("");
                System.exit(1);
            }
        }

        ArrayList<Channel> channels = new ArrayList<>();
        for (String target : targets) {
            channels.add(ManagedChannelBuilder.forTarget(target)
                    .usePlaintext()
                    .build());
        }

        try {
            StorageClient2Channels client = new StorageClient2Channels(channels);
            for (int i = 0; i <= 9; i++) {
                if (i % 2 == 0) {
                    client.write(client.generator.nextInt(10000), client.blockingStubPrimary);
                }
                if (i % 2 != 0) {
                    client.write(client.generator.nextInt(10000), client.blockingStubSecondary);
                }
            }

            Empty emptyRequest = new Empty();
            ReadAllReply primaryCopy = client.blockingStubPrimary.readAll(emptyRequest);
            ReadAllReply secondaryCopy = client.blockingStubSecondary.readAll(emptyRequest);

            logger.info("Data dump (Primary): " + primaryCopy);
            logger.info("Data dump (Secondary): " + secondaryCopy);

        } finally {
            ManagedChannel tmpPrimary = (ManagedChannel) channels.get(0);
            ManagedChannel tmpSecondary = (ManagedChannel) channels.get(1);
            tmpPrimary.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            tmpSecondary.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}*/
