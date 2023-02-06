
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
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
    private final ArrayList<NodeClient> targetsClient = new ArrayList<>();
    private NodeStatus status = NodeStatus.NODE;
    private ServerData leaderTarget;
    private boolean isUnderElection = false;
    private int leaderLiveCountDownPeriod = 5;
    private final int leaderHealthCheckSendPeriod = 2;
    private final int electionTerminatePeriod = 5;
    ScheduledThreadPoolExecutor leadreHealthCheckThread = new ScheduledThreadPoolExecutor(1);
    private static final Logger logger = Logger.getLogger(NodeServer.class.getName());
    private final ServerData thisServerData;
    private final ArrayList<ServerData> allServersData;
    static final String serverIP = "localhost";

    private Server server;

    public NodeServer(ServerData serverData, ArrayList<ServerData> allServersData) {
        this.thisServerData = serverData;
        this.allServersData = allServersData;
        initElectionClient(thisServerData, allServersData);
    }

    public Server start() throws IOException {
        server = ServerBuilder.forPort(thisServerData.getPort())
                .addService(new ElectionImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + thisServerData.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            try {
                NodeServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println("*** server shut down");
        }));
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
        targetsClient.clear();
        for (ServerData serverInstData : allServersData) {
            if (serverInstData.getPort() == thisServerData.getPort()) continue;
            String target =  serverIP + ":" + serverInstData.getPort();
            NodeClient client = new NodeClient(target, this);
            targetsClient.add(client);
        }
        Runnable periodicWork = () -> {
            for (NodeClient nodeClient : targetsClient) {
                nodeClient.leaderHealthTrigger(thisServerData);
            }
        };
        leadreHealthCheckThread.scheduleAtFixedRate(periodicWork, 0, leaderHealthCheckSendPeriod, SECONDS);
    }

    private void stop() throws InterruptedException {
        if (server == null) return;
        targetsClient.clear();
        leadreHealthCheckThread.shutdownNow();
        server.shutdownNow();
    }

    private void initElectionClient(ServerData thisServerData, ArrayList<ServerData> allServersData) {
        for (ServerData serverInstData : allServersData) {
            if (serverInstData.getId() <= thisServerData.getId()) continue;
            String target = serverIP + ":" + serverInstData.getPort();
            NodeClient client = new NodeClient(target, this);
            targetsClient.add(client);
        }
    }

    private void startElection() {
        isUnderElection = true;
        leaderTarget = null;
        status = NodeStatus.LEADER;
        for (NodeClient client : targetsClient) {
            client.electionTrigger(thisServerData.getId());
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
        logger.info("i " + thisServerData.getId() + " received response from " + targer);
        if (response.hasOkMessage()) {
            logger.info("i " + thisServerData.getId() + " am not a leader");
            status = NodeStatus.NODE;
        }
    }

    class ElectionImpl extends LeaderElectionGrpc.LeaderElectionImplBase {

        @Override
        public void election(ElectionRequest request, StreamObserver<ElectionResponse> responseObserver) {
            try {
                Runnable runnable = () -> {
                    if (request.getServerId() > thisServerData.getId()) {
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
            leaderTarget = new ServerData(request.getPort(), request.getId(), request.getUrl());
            logger.info("I am " + thisServerData.getUrl() + " The leader id is: " + leaderTarget.getId());
            leaderLiveCountDownPeriod = 5;
        }

        @Override
        public void leaderKill(LeaderKillRequest request, StreamObserver<Empty> responseObserver) {
            responseObserver.onCompleted();
            try {
                if (request.getLeaderId() == thisServerData.getId()) stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void giveTheLeader(Empty request, StreamObserver<LeaderHealthCheckInfo> responseObserver) {
            if (leaderTarget == null) {
                responseObserver.onError(Status.INTERNAL.withDescription("No leader election has been finisehd").asException());
                return;
            }
            LeaderHealthCheckInfo response = LeaderHealthCheckInfo
                    .newBuilder()
                    .setPort(leaderTarget.getPort())
                    .setId(leaderTarget.getId())
                    .setUrl(leaderTarget.getUrl())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}