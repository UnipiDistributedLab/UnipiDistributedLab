package leaderelection;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.servers.BullyElection.*;
import models.leaderelection.ServerInfo;

import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NodeClient {

    interface NodeClientListener {
        void receivedElectionMessage(ElectionResponse response);
    }

    private static final Logger logger = Logger.getLogger(NodeClient.class.getName());

    private ServerInfo connectionServer;
    private final LeaderElectionGrpc.LeaderElectionBlockingStub connectionStub;
    private WeakReference<NodeClientListener> mListener;

    /**
     *
     * @param connectionServer the target server of connection
     */
    public NodeClient(ServerInfo connectionServer, NodeClientListener listener) {
        this.connectionServer = connectionServer;
        this.mListener = new WeakReference<>(listener);
        Channel readChannel = ManagedChannelBuilder
                .forTarget(connectionServer.getUrl())
                .usePlaintext()
                .build();
        connectionStub = LeaderElectionGrpc.newBlockingStub(readChannel);
    }

    /**
     *
     * @param serverUrl current server url
     * @param serverId current server id
     */
    public void electionMessage(String serverUrl, int serverId) {
        Runnable taskRunnable = () -> {
            ElectionRequest request = ElectionRequest
                    .newBuilder()
                    .setUrl(serverUrl)
                    .setId(serverId)
                    .build();
            ElectionResponse response = connectionStub.election(request);
            if (mListener.get() != null) mListener.get().receivedElectionMessage(response);

            // Prosthesa auto to log gia na einai emfanes sto console to result
//            String log = "Server id: " + serverId + " result is: " + response.getOk() + " from server " + connectionServer.getId();
//            logger.info(log);
        };
        Thread thread = new Thread(taskRunnable);
        thread.start();

        // An kanete comment in to join tha deite oti diathrei h seira twn request/response
        // kati to opoio den mas endiaferei sthn dikh mas ulopoihsh

//        try {
//            thread.join();
//        } catch (Exception e) {
//            logger.info(e.getMessage());
//        }
    }

    public void leaderHealthCheck(ServerInfo serverInfo) {
        Runnable taskRunnable = () -> {
            LeaderHealthCheckInfo request = LeaderHealthCheckInfo
                    .newBuilder()
                    .setNodeIP(serverInfo.getUrl())
                    .setId(serverInfo.getId())
                    .setPort(serverInfo.getPort())
                    .build();
            NodeHealthCheckResponseInfo response = connectionStub.leaderHealthCheck(request);
            // Prosthesa auto to log gia na einai emfanes sto console to result
//            String log = "Server id: " + serverId + " result is: " + response.getOk() + " from server " + connectionServer.getId();
//            logger.info(log);
        };
        Thread thread = new Thread(taskRunnable);
        thread.start();
    }

    public ServerInfo getConnectionServer() {
        return connectionServer;
    }
}
