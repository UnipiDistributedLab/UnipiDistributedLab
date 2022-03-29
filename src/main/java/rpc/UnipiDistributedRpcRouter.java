package rpc;

import conformers.RoutingControllerConformer;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class UnipiDistributedRpcRouter implements RoutingControllerConformer {
    public void startRouting() {
        Server server = ServerBuilder
                .forPort(8080)
                .addService(new AuthLoginImpl())
                .build();

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
