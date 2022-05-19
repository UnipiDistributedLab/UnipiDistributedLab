package client;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.servers.LamportClock.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StorageClientReadYourWrite {
    private static final Logger logger = Logger.getLogger(StorageClientReadYourWrite.class.getName());

    private final ValueStoreGrpc.ValueStoreBlockingStub blockingStubPrimary;
    private final ValueStoreGrpc.ValueStoreBlockingStub blockingStubSecondary;
    private LamportClock clock = new LamportClock(-1);
    private Random generator = new Random();


    public StorageClientReadYourWrite(ArrayList<Channel> channel) {
        blockingStubPrimary = ValueStoreGrpc.newBlockingStub(channel.get(0));
        blockingStubSecondary = ValueStoreGrpc.newBlockingStub(channel.get(1));
    }

    public int write(int value, ValueStoreGrpc.ValueStoreBlockingStub blockingStub) {
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
            clock.tick(response.getCounter());
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            //TODO think twice because resets get clock
            return 0;
        }
        logger.info("Received: " + response.getCounter() + ":" + response.getTimestamp() + ":" + response.getValue());
        return response.getCounter();
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
            StorageClientReadYourWrite client = new StorageClientReadYourWrite(channels);
            int value = client.generator.nextInt(10000);
//            for (int i = 0; i <= 9; i++) {
//                if (i % 2 == 0) {
                int lamportClock = client.write(value, client.blockingStubPrimary);
//                }
//                if (i % 2 != 0) {
//            client.write(client.generator.nextInt(10000), client.blockingStubSecondary);
            ReadRequest readIdRequest = ReadRequest
                    .newBuilder()
                    .setValue(value)
                    .setCounter(lamportClock)
                    .build();
            ReadReply response = client.blockingStubSecondary.read(readIdRequest);
            logger.info("Read data"+ response.getValue());
//                }
//            }

            Empty emptyRequest = new Empty();
            try {
                ReadAllReply primaryCopy = client.blockingStubPrimary.readAll(emptyRequest);
                ReadAllReply secondaryCopy = client.blockingStubSecondary.readAll(emptyRequest);
                logger.info("Data dump (Primary): " + primaryCopy.getData());
                logger.info("Data dump (Secondary): " + secondaryCopy.getData());
            } catch (StatusRuntimeException e) {
                logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
                return;
            }

        } finally {
            ManagedChannel tmpPrimary = (ManagedChannel) channels.get(0);
            ManagedChannel tmpSecondary = (ManagedChannel) channels.get(1);
            tmpPrimary.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            tmpSecondary.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
