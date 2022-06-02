
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.unipi.election.ElectionRequest;
import io.grpc.unipi.election.ElectionResponse;
import io.grpc.unipi.election.LeaderElectionGrpc;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

enum NodeStatus {
    LEADER, NODE
}

public class NodeServer {
    private List<Integer> serversIds = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
    private ArrayList<String> targets = new ArrayList<>();
    private NodeStatus status = NodeStatus.NODE;
    private int id;
    private static final Logger logger = Logger.getLogger(NodeServer.class.getName());
    private final Integer defaultPort = 50051;

    private Server server;

    public NodeServer(Integer id) {
        this.id = id;
        initElectionClient(id);
    }

    public Server start() throws IOException {
        /* The port on which the server should run */
        int port = defaultPort + id;
        server = ServerBuilder.forPort(port)
                .addService(new ElectionImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    NodeServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
        return  server;
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private void initElectionClient(Integer id) {
        for (Integer serverId : serversIds) {
            if (serverId < id) continue;
            Integer port = defaultPort + serverId;
            targets.add("localhost:" + port);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    class ElectionImpl extends LeaderElectionGrpc.LeaderElectionImplBase {

        @Override
        public void election(ElectionRequest request, StreamObserver<ElectionResponse> responseObserver) {
            super.election(request, responseObserver);
            @Nullable boolean okMessage = request.getServerId() > id ? true : null;
            ElectionResponse response = ElectionResponse.newBuilder().setOkMessage(okMessage).build();
            responseObserver.onNext(response);
        }
    }
}