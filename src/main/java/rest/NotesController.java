package rest;

import com.google.gson.Gson;
import conformers.RoutingControllerConformer;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.servers.LamportClock.*;
import models.lamport.ReadApiRequest;
import models.lamport.ReadApiResponse;
import models.lamport.WriteApiRequest;
import models.lamport.WriteApiResponse;
import storage.InMemoryStorage;

import static spark.Spark.post;

public final class NotesController implements RoutingControllerConformer {
    private Gson gson;

    private WriteValueStorageGrpc.WriteValueStorageBlockingStub writeValueStorageStub;
    private ReadValueStorageGrpc.ReadValueStorageBlockingStub readValueStorageStub;
    private InMemoryStorage readStorage = new InMemoryStorage();
    public NotesController(Gson gson) {
        this.gson = gson;
    }
    public void startRouting() {
        initStorageCommunications();
        post("/save", (req, res) -> {
            WriteApiRequest clientWriteRequest = gson.fromJson(req.body(), WriteApiRequest.class);
            WriteRequest request = WriteRequest
                    .newBuilder()
                    .setValue(clientWriteRequest.value)
                    .build();
            WriteResponse rpcResponse = writeValueStorageStub.doWrite(request);
            WriteApiResponse apiResponse = new WriteApiResponse(rpcResponse.getTimeStamp());
            return gson.toJson(apiResponse);
        });

        post("/read", (req, res) -> {
            ReadApiRequest clientReadRequest = gson.fromJson(req.body(), ReadApiRequest.class);
            ReadRequest request = ReadRequest
                    .newBuilder()
                    .setTimeStamp(clientReadRequest.timeStamp)
                    .build();
            ReadResponse rpcResponse = readValueStorageStub.doRead(request);
            ReadApiResponse apiResponse = new ReadApiResponse(rpcResponse.getValue());
            return gson.toJson(apiResponse);
        });
    }

    private void initStorageCommunications() {
        Channel writeChannel = ManagedChannelBuilder
                .forAddress("localhost", 5010)
                .usePlaintext()
                .build();
        writeValueStorageStub = WriteValueStorageGrpc.newBlockingStub(writeChannel);


        Channel readChannel = ManagedChannelBuilder
                .forAddress("localhost", 2434)
                .usePlaintext()
                .build();
        readValueStorageStub = ReadValueStorageGrpc.newBlockingStub(readChannel);
    }
}