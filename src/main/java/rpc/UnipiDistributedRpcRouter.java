package rpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class UnipiDistributedRpcRouter {
    public void startRouting() {
        Server server = ServerBuilder
                .forPort(8080)
                .addService(new DistirbutedProto()).build();

        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
