
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.unipi.election.*;

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
    public static List<Integer> serversIds = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    private ArrayList<String> targets = new ArrayList<>();
    private HashMap<String, NodeClient> targetsClientsMap = new HashMap();
    private NodeStatus status = NodeStatus.NODE;
    private String leaderTarget;
    private boolean isUnderElection = false;
    private Integer leaderLiveCountDownPeriod = 5;
    private final int currentServerId;
    private final int leaderHealthCheckSendPeriod = 2;
    private final int electionTerminatePeriod = 15;
    ScheduledThreadPoolExecutor leadreHealthCheckThread = new ScheduledThreadPoolExecutor(1);
    private static final Logger logger = Logger.getLogger(NodeServer.class.getName());
    private final Integer defaultPort = 50051;
    //HashMap boolean means that one server send OK and has bigger id
//    private HashMap<String, Boolean> pendingTargetsResponses = new HashMap<>();

    private Server server;

    public NodeServer(Integer id) {
        this.currentServerId = id;
        initElectionClient(id);
    }

    public Server start() throws IOException {
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
        return server;
    }

    private void startPeriodicCheck() {
        if (status == NodeStatus.LEADER) return;
        ScheduledThreadPoolExecutor carrierThread = new ScheduledThreadPoolExecutor(1);
        Runnable periodicWork = () -> {
            if (leaderLiveCountDownPeriod == 0) {
                if (isUnderElection) {
                    return;
                }
                logger.info(" Start election no leader exist");
                startElection();
                electionTimeOut();
                return;
            }
            System.out.println(leaderLiveCountDownPeriod);
            leaderLiveCountDownPeriod--;
        };
        carrierThread.scheduleAtFixedRate(periodicWork, 0, 1, SECONDS);
//        carrierThread.scheduleAtFixedRate(periodicWork, 2, period, TimeUnit.SECONDS);
    }

    private void electionTimeOut() {
        ScheduledThreadPoolExecutor carrierThread = new ScheduledThreadPoolExecutor(1);
        Runnable periodicWork;
        periodicWork = () -> {
            isUnderElection = false;
            this.leaderLiveCountDownPeriod = 5;
            if (status == NodeStatus.LEADER) addLeaderHealthCheck();
        };
        carrierThread.schedule(periodicWork, electionTerminatePeriod, TimeUnit.SECONDS);
    }

    private void addLeaderHealthCheck() {
        targets.clear();
        targetsClientsMap.clear();
        for (Integer serverId : serversIds) {
            if (serverId == currentServerId) continue;
            Integer port = defaultPort + serverId;
            String target = "localhost:" + port;
            targets.add(target);
            NodeClient client = new NodeClient(target, this);
            targetsClientsMap.put(target, client);
        }
        Runnable periodicWork = () -> {
            Integer port = defaultPort + currentServerId;
            String leaderTarget = "localhost:" + port;
            for (String target : targets) {
                NodeClient client = targetsClientsMap.get(target);
                client.leaderHealthTrigger(leaderTarget);
            }
        };
        leadreHealthCheckThread.scheduleAtFixedRate(periodicWork, 0, leaderHealthCheckSendPeriod, SECONDS);
    }

    private void stop() throws InterruptedException {
        if (server == null) return;
        targetsClientsMap.clear();
        leadreHealthCheckThread.shutdownNow();
        server.shutdownNow();
    }

    private void initElectionClient(Integer currentServerId) {
        for (Integer serverId : serversIds) {
            if (serverId <= currentServerId) continue;
            Integer port = defaultPort + serverId;
            String target = "localhost:" + port;
            targets.add(target);
            NodeClient client = new NodeClient(target, this);
            targetsClientsMap.put(target, client);
        }
    }

    private void startElection() {
        isUnderElection = true;
        status = NodeStatus.LEADER;
        for (String target : targets) {
            NodeClient client = targetsClientsMap.get(target);
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
        logger.info("i " + currentServerId + " received response from " + targer);
        if (response.hasOkMessage()) {
            logger.info("i " + currentServerId + " am not a leader");
            status = NodeStatus.NODE;
        }
    }

    class ElectionImpl extends LeaderElectionGrpc.LeaderElectionImplBase {

        @Override
        public void election(ElectionRequest request, StreamObserver<ElectionResponse> responseObserver) {
            try {
                Runnable runnable = () -> {
                    if (request.getServerId() > currentServerId) {
                        ElectionResponse response = ElectionResponse.newBuilder().build();
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                        return;
                    }
                    ElectionResponse response = ElectionResponse.newBuilder().setOkMessage(true).build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                };
                ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
                scheduler.schedule(runnable, 0, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
        }

        @Override
        public void leaderHealthCheck(LeaderHealthCheckInfo request, StreamObserver<Empty> responseObserver) {
            leaderTarget = request.getTarget();
            logger.info("Long live the leader " + leaderTarget);
            leaderLiveCountDownPeriod = 5;
        }

        @Override
        public void leaderKill(LeaderKillRequest request, StreamObserver<Empty> responseObserver) {
            responseObserver.onCompleted();
            try {
                if (request.getLeaderId() == currentServerId) stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}