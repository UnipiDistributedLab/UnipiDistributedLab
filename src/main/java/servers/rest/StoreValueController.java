package servers.rest;

import com.google.gson.Gson;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.servers.LamportClock.*;
import servers.MainValueStore;
import servers.StorageType;
import servers.ValueStorage;
import servers.rest.request.ReadValueResponse;
import servers.rest.request.WriteValueRequest;
import servers.rest.request.WriteValueResponse;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

import static spark.Spark.get;
import static spark.Spark.post;

public class StoreValueController {

    private ValueStoreGrpc.ValueStoreBlockingStub writeBlockingStub;
    private ValueStoreGrpc.ValueStoreBlockingStub readBlockingStub;
    private final Logger logger = Logger.getLogger(MainValueStore.class.getName());
    private static final Gson mGson = new Gson();

    public void startRouting() throws InterruptedException {
        initRestInterface();
        initRPC();
    }

    private void initRestInterface() {
        post("api/write", (req, res) -> {
            WriteValueRequest request = mGson.fromJson(req.body(), WriteValueRequest.class);
            if (request == null) return "Wrong Request";
            int clock = write(request.getValue(), request.getLamportCounter());
            WriteValueResponse response = new WriteValueResponse(clock);
            return mGson.toJson(response);
        });
        get("api/read", (req, res) -> {
            String clock = req.queryParams("clock");
            if (clock == null) return "Missing params";
            ReadValueResponse response = new ReadValueResponse(read(clock));
            return mGson.toJson(response);
        });
    }

    private void initRPC() throws InterruptedException {
        writeBlockingStub = ValueStoreGrpc.newBlockingStub(
                ManagedChannelBuilder.forTarget("localhost:50051")
                        .usePlaintext()
                        .build());
        readBlockingStub = ValueStoreGrpc.newBlockingStub(
                ManagedChannelBuilder.forTarget("localhost:50052")
                        .usePlaintext()
                        .build());

        Runnable initWriteHost = () -> {
            final ValueStorage primaryServer = new ValueStorage(StorageType.WRITE, "localhost:50052");
            try {
                primaryServer.start(50051);
                primaryServer.blockUntilShutdown();
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        Thread writeThread = new Thread(initWriteHost);
        Thread readThread = new Thread(initReadHost);

        writeThread.start();
        readThread.start();

        writeThread.join();
        readThread.join();
        System.out.println("All threads have successfully terminated");
    }

    private int write(String value, int lamportCounter) {
        logger.info("Sending " + value + "to server" + writeBlockingStub.getChannel().toString() + "\n");
        String realtime = new Timestamp(System.currentTimeMillis()).toString();

        WriteRequest request = WriteRequest.newBuilder()
                .setValue(value)
                .setCounter(lamportCounter)
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

    private String read(String lamportClock) {
        ReadRequest readIdRequest = ReadRequest
                .newBuilder()
                .setCounter(Integer.parseInt(lamportClock))
                .build();
        ReadReply response = readBlockingStub.read(readIdRequest);
        return response.getValue();
    }
}
