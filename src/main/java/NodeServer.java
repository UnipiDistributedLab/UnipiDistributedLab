
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.unipi.election.ElectionRequest;
import io.grpc.unipi.election.ElectionResponse;
import io.grpc.unipi.election.LeaderElectionGrpc;
import org.checkerframework.checker.units.qual.A;
import org.w3c.dom.Node;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.SECONDS;

enum NodeStatus {
    LEADER, NODE
}

public class NodeServer implements NodeClient.NodeClientListener {
    private List<Integer> serversIds = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
    private ArrayList<String> targets = new ArrayList<>();
    private HashMap<String, NodeClient> targetsClientsMap = new HashMap();
    private NodeStatus status = NodeStatus.NODE;
    private String leaderTarget;
    private Integer period = 5;
    private final int currentServerId;
    private static final Logger logger = Logger.getLogger(NodeServer.class.getName());
    private final Integer defaultPort = 50051;
    private ArrayList<String> pendingTargetsResponses = new ArrayList();

    private Server server;

    public NodeServer(Integer id) {
        this.currentServerId = id;
        initElectionClient(id);
    }

    public Server start() throws IOException {
        /* The port on which the server should run */
        int port = defaultPort + currentServerId;
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
        startPeriodicCheck();
        return  server;
    }

    private void  startPeriodicCheck() {
        ScheduledThreadPoolExecutor carrierThread = new ScheduledThreadPoolExecutor(1);
        Runnable periodicWork;
        periodicWork = new Runnable() {
            public void run() {

                System.out.println(period);
                period--;

                if (period < 0) {
                    System.out.println("Timer Over!");
                    carrierThread.shutdown();
                }
                if (period == 0) {
                    logger.info(" Start election no leader exist");
                }
            }
        };
        carrierThread.scheduleAtFixedRate(periodicWork, 0, 1, SECONDS);
        startElection();
//        carrierThread.scheduleAtFixedRate(periodicWork, 2, period, TimeUnit.SECONDS);
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, SECONDS);
        }
    }

    private void initElectionClient(Integer currentServerId) {
        for (Integer serverId : serversIds) {
            if (serverId < currentServerId) continue;
            Integer port = defaultPort + serverId;
            String target = "localhost:" + port;
            targets.add(target);
            NodeClient client = new NodeClient(target, this);
            targetsClientsMap.put(target, client);
        }
    }

    private void startElection() {
        for (String target: targets) {
            NodeClient client = targetsClientsMap.get(target);
            pendingTargetsResponses.add(target);
            client.electionTrigger(currentServerId);
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

    @Override
    public void receveidResponseFrom(String targer, ElectionResponse response) {
        logger.info("i " + currentServerId + "received response from " + targer);
        pendingTargetsResponses.remove(targer);
    }

    class ElectionImpl extends LeaderElectionGrpc.LeaderElectionImplBase {

        @Override
        public void election(ElectionRequest request, StreamObserver<ElectionResponse> responseObserver) {
            super.election(request, responseObserver);
            @Nullable boolean okMessage = request.getServerId() > currentServerId ? true : null;
            ElectionResponse response = ElectionResponse.newBuilder().setOkMessage(okMessage).build();
            responseObserver.onNext(response);
        }
    }
}