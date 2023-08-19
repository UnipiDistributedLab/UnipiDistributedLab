package leaderelection;

import conformers.RoutingControllerConformer;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.examples.servers.BullyElection.ElectionResponse;
import io.grpc.examples.servers.BullyElection.LeaderHealthCheckInfo;
import models.leaderelection.ServerInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

enum NodeStatus {
    LEADER, NODE
}


public final class NodeServer implements RoutingControllerConformer, NodeClient.NodeClientListener, LeaderElectionServer.LeaderElectionServerListener  {

    private static final Logger logger = Logger.getLogger(NodeServer.class.getName());
    private NodeStatus status = NodeStatus.NODE;
    private ServerInfo serverInfo;
    private ServerInfo leaderInfo;
    private Server server;
    private ArrayList<NodeClient> clients = new ArrayList<>();
    private int leaderSecondsCounter = 0;
    private final int healthCheckThreashold = 5;
    private AtomicBoolean isUnderElection = new AtomicBoolean(false);

    public NodeServer(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    @Override
    public void startRouting() {
        server = ServerBuilder
                .forPort(serverInfo.getPort())
                .addService(new LeaderElectionServer(serverInfo, this))
                .build();
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Runnable serverRunnable = () -> {
            try {
                server.awaitTermination();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Thread serverThread = new Thread(serverRunnable);
        serverThread.start();
//        try {
//            serverThread.join();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }

    public void startPeriodicLeaderCheck() {
        ScheduledThreadPoolExecutor healthCheckThread = new ScheduledThreadPoolExecutor(1);
        Runnable periodicRunnable = () -> {
            if (leaderSecondsCounter > healthCheckThreashold) {
                if (isUnderElection.get()) return;
                startElection();
                electionTimeOut();
                return;
            }
            leaderSecondsCounter++;
        };
        healthCheckThread.scheduleAtFixedRate(periodicRunnable, 3, 1, TimeUnit.SECONDS);
    }

    public void startElection() {
        logger.info("For server" + serverInfo.getUrl() + " election started");
        isUnderElection.set(true);
        status = NodeStatus.LEADER;
        for (NodeClient client : clients) {
            client.electionMessage(serverInfo.getUrl(), serverInfo.getId());
        }
    }

    public void addOtherServers(ArrayList<ServerInfo> otherServers){
        for (ServerInfo otherServer : otherServers) {
            addOtherServer(otherServer);
        }
    }

    public void addOtherServer(ServerInfo otherServer) {
        NodeClient client = new NodeClient(otherServer, this);
        clients.add(client);
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    @Override
    public void receivedElectionMessage(ElectionResponse response) {
        if (response.getOk()) status =  NodeStatus.NODE;
    }

    @Override
    public void leaderHealthCheck(LeaderHealthCheckInfo request) {
        leaderSecondsCounter = 0;
    }

    private void electionTimeOut() {
        ScheduledThreadPoolExecutor electionTimeOutThread = new ScheduledThreadPoolExecutor(1);
        Runnable runnable = () -> {
            isUnderElection.set(false);
            leaderSecondsCounter = 0;
            electionTimeOutThread.shutdown();
            if (status == NodeStatus.LEADER) {
                leaderHealthCheckRoutine();
            }
        };
        electionTimeOutThread.schedule(runnable, 5, TimeUnit.SECONDS);
    }

    private void leaderHealthCheckRoutine() {
        ScheduledThreadPoolExecutor leaderHealthCheckThread = new ScheduledThreadPoolExecutor(1);
        Runnable periodicRunnable = () -> {
           for (NodeClient nodeClient: clients) {
               if (nodeClient.getConnectionServer().getId() == serverInfo.getId()) return;
               nodeClient.leaderHealthCheck(serverInfo);
           }
        };
        leaderHealthCheckThread.scheduleAtFixedRate(periodicRunnable, 3, 1, TimeUnit.SECONDS);
    }
}