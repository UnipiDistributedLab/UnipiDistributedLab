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
        Server server2 = ServerBuilder
                .forPort(5010)
                .addService(new WriteValueStorageImpl())
                .build();

        Server server3 = ServerBuilder
                .forPort(2434)
                .addService(new ReadValueStorageImpl())
                .build();
        try {
            server.start();
            server2.start();
            server3.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Runnable server1Runnable = () -> {
            try {
                server.awaitTermination();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Runnable server2Runnable = () -> {
            try {
                server2.awaitTermination();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Runnable server3Runnable = () -> {
            try {
                server3.awaitTermination();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Thread server1th = new Thread(server1Runnable);
        Thread server2th = new Thread(server2Runnable);
        Thread server3th = new Thread(server3Runnable);
        server1th.start();
        server2th.start();
        server3th.start();
//        try {
//            server1th.join();
//            server2th.join();
//            server3th.join();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }
}
