package servers.leaderelection;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.grpc.unipi.election.*;
import servers.TimeOutConfigParams;
import utlis.Atomic;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class NodeServer implements NodeClient.NodeClientListener {

    public interface NodeServerListener {
        void leaderUpdate(@Nullable ServerData leader);
        void stopStorageServer(ServerData leader);
        void leaderKilled();
    }

    private final ArrayList<NodeClient> targetsClient = new ArrayList<>();
    private NodeStatus status = NodeStatus.NODE;
    private ServerData leaderTarget;
    private boolean isUnderElection = false;
    private Atomic<Integer> leaderLiveCountDownPeriod = new Atomic<>();
    private final Long leaderHeartBeatPeriodMs = TimeOutConfigParams.shared().getLeaderHeartBeatPeriodMS();
    private final Long mServerShutdownTimeoutMs = TimeOutConfigParams.shared().getServerShutdownTimeoutMs();
    private final Long electionTimeOutPeriodMs = TimeOutConfigParams.shared().getElectionTimeOutPeriodMS();
    ScheduledThreadPoolExecutor leadreHealthCheckThread = new ScheduledThreadPoolExecutor(1);
    private static final Logger logger = Logger.getLogger(NodeServer.class.getName());
    private final ServerData thisServerData;
    private final ArrayList<ServerData> allServersData;
    private Server server;
    private ScheduledThreadPoolExecutor healtchCheckThread;
    private WeakReference<NodeServerListener> mListener;

    public NodeServer(ServerData serverData, ArrayList<ServerData> allServersData, NodeServerListener listener) {
        this.mListener = new WeakReference(listener);
        this.leaderLiveCountDownPeriod.set(5);
        this.thisServerData = serverData;
        this.allServersData = allServersData;
        initElectionClient(thisServerData, allServersData);
    }

    public void addElectionService(ServerBuilder builder) throws IOException {
//        logger.info("Server started, listening on " + thisServerData.getPort());
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
        builder.addService(new ElectionImpl());
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void startPeriodicCheck() {
        if (status == NodeStatus.LEADER) return;
        healtchCheckThread = new ScheduledThreadPoolExecutor(1);
        Runnable periodicWork = () -> {
            if (status == NodeStatus.LEADER) return;
            if (leaderLiveCountDownPeriod.get() == 0) {
                if (isUnderElection) return;
                logger.info(" Start election no leader exist");
                startElection();
                electionTimeOut();
                return;
            }
            System.out.println(leaderLiveCountDownPeriod);
            leaderLiveCountDownPeriod.set(leaderLiveCountDownPeriod.get() - 1);
        };
        if (leaderLiveCountDownPeriod.get() == 0) {
            return;
        }
        healtchCheckThread.scheduleAtFixedRate(periodicWork, 3, 1, SECONDS);
    }

    private void electionTimeOut() {
        ScheduledThreadPoolExecutor carrierThread = new ScheduledThreadPoolExecutor(1);
        Runnable periodicWork;
        periodicWork = () -> {
            isUnderElection = false;
            this.leaderLiveCountDownPeriod.set(5);
            if (status == NodeStatus.LEADER) {
                Thread thread = new Thread(() -> {
                    if (mListener.get() != null) {
                        mListener.get().stopStorageServer(thisServerData);
                        mListener.get().leaderUpdate(this.thisServerData);
                    }
                });
                thread.start();
                addLeaderHealthCheck();
            }
            carrierThread.shutdownNow();
        };
        carrierThread.schedule(periodicWork, electionTimeOutPeriodMs, TimeUnit.MILLISECONDS);
    }

    /**
     * This function called only from leader in order to set a health check periodic trigger
     */
    private void addLeaderHealthCheck() {
        leadreHealthCheckThread.shutdownNow();
        leadreHealthCheckThread = new ScheduledThreadPoolExecutor(1);
        for (NodeClient client : targetsClient) {
            client.shutdownConnectionChannel();
        }
        targetsClient.clear();
        for (ServerData serverInstData : allServersData) {
            if (serverInstData.getGrPcPort() == thisServerData.getGrPcPort()) continue;
            NodeClient client = new NodeClient(serverInstData, this);
            targetsClient.add(client);
        }
        Runnable periodicWork = () -> {
//            logger.log(Level.WARNING, "I am in addLeaderHealthCheck");
            for (NodeClient nodeClient : targetsClient) {
                if (nodeClient.getData().getId() == thisServerData.getId()) continue;
                if (nodeClient.getData().getId() >= thisServerData.getId()) continue;
                nodeClient.leaderHealthTrigger(thisServerData);
            }
        };
        leadreHealthCheckThread.scheduleAtFixedRate(periodicWork, 0, leaderHeartBeatPeriodMs, MILLISECONDS);
    }

    private void stop() throws InterruptedException {
        Runnable runnable = () -> {
            if (server == null) return;
            for (NodeClient client : targetsClient) {
                client.shutdownConnectionChannel();
            }
            targetsClient.clear();
            leadreHealthCheckThread.shutdownNow();
            server.shutdownNow();
            try {
                server.awaitTermination(mServerShutdownTimeoutMs, MILLISECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } finally {
                server.shutdownNow();
            }
            healtchCheckThread.shutdown();
            if (mListener.get() != null) mListener.get().leaderKilled();
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private void initElectionClient(ServerData thisServerData, ArrayList<ServerData> allServersData) {
        for (ServerData serverInstData : allServersData) {
            if (serverInstData.getId() == thisServerData.getId()) continue;
            NodeClient client = new NodeClient(serverInstData, this);
            targetsClient.add(client);
        }
    }

    private void startElection() {
        if(leaderTarget != null) {
            int leaderIndex = allServersData.indexOf(leaderTarget);
            if (leaderIndex >= 0) {
                allServersData.remove(leaderIndex);
                if (targetsClient.size() -1 >= leaderIndex) targetsClient.remove(leaderIndex);
            }
        }
        isUnderElection = true;
        leaderTarget = null;
        status = NodeStatus.LEADER;
        for (NodeClient client : targetsClient) {
            client.electionTrigger(thisServerData.getId());
        }
        mListener.get().leaderUpdate(null);
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
//            server.awaitTermination();
        }
    }

    @Override
    public void receveidResponseFrom(String targer, ElectionResponse response) {
        logger.info("receveidResponseFrom i " + thisServerData.getId() + " received response from " + targer);
        if (response.hasOkMessage()) {
            logger.info("receveidResponseFrom i " + thisServerData.getId() + " am not a leader");
            status = NodeStatus.NODE;
        }
    }

    /**
     * Leader election gRPC methods
     */
    class ElectionImpl extends LeaderElectionGrpc.LeaderElectionImplBase {

        @Override
        public void election(ElectionRequest request, StreamObserver<ElectionResponse> responseObserver) {
            try {
                ElectionResponse.Builder responseBuilder = ElectionResponse.newBuilder();
                Runnable runnable = () -> {
                    if (request.getServerId() > thisServerData.getId()) {
                        responseObserver.onNext(responseBuilder.build());
                        responseObserver.onCompleted();
                        return;
                    }
                    ElectionResponse response = responseBuilder.setOkMessage(true).build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                };
                Thread thread = new Thread(runnable);
                thread.start();
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
        }

        @Override
        public void heartBeat(LeaderHealthCheckInfo request, StreamObserver<Empty> responseObserver) {
            leaderTarget = new ServerData(request.getGrPcPort(), request.getApiPort(), request.getId(),
                    request.getUrl());
            if (mListener.get() != null) {
                mListener.get().leaderUpdate(leaderTarget);
            }
            if (status == NodeStatus.LEADER && thisServerData.getId() < request.getId()) {
                status = NodeStatus.NODE;
            }
            logger.info("I am " + thisServerData.getUrl() + " The leader id is: " + leaderTarget.getId());
            leaderLiveCountDownPeriod.set(5);
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        }

        @Override
        public void leaderKill(LeaderKillRequest request, StreamObserver<Empty> responseObserver) {
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
            try {
                if (request.getLeaderId() == thisServerData.getId()) stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void giveTheLeader(Empty request, StreamObserver<LeaderHealthCheckInfo> responseObserver) {
            if (mListener.get() != null) {
                mListener.get().leaderUpdate(leaderTarget);
            }
            if (leaderTarget == null) {
                responseObserver.onError(Status.INTERNAL.withDescription("No leader election has been finisehd").asException());
                return;
            }
            LeaderHealthCheckInfo response = LeaderHealthCheckInfo
                    .newBuilder()
                    .setGrPcPort(leaderTarget.getGrPcPort())
                    .setApiPort(leaderTarget.getApiPort())
                    .setId(leaderTarget.getId())
                    .setUrl(leaderTarget.getUrl())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}