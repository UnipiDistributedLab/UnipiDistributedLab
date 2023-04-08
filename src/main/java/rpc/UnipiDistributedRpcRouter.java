package rpc;

import conformers.RoutingControllerConformer;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class UnipiDistributedRpcRouter implements RoutingControllerConformer {

    public void startRouting() {
        Server server = ServerBuilder
                .forPort(8083)
                .addService(new AuthLoginImpl())
                .addService(new StreamServiceImpl())
                .addService(new StreamTestImpl())
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
