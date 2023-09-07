package servers.leaderelection;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.unipi.election.*;

import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NodeClient {

    public interface NodeClientListener {
        void receveidResponseFrom(String targer, ElectionResponse response);
    }

    private static final Logger logger = Logger.getLogger(NodeClient.class.getName());
    private final LeaderElectionGrpc.LeaderElectionBlockingStub blockingStub;
    private ServerData data;
    private WeakReference<NodeClientListener> mListener;
    private ManagedChannel channel;

    /**
     * There is 1:1 matching of other hosting server and node client
     * each servers.HostServer keeps a list with nodeclients which is  1:1 pointing to other HostServers (nodes) for example:
     * for 10 servers.HostServer the first servers.HostServer keeps 9 NodeClients for the 9 other HostServers
     * @param data target servers.HostServer
     * @param listener in order to inform about the response on leader election process
     */
    public NodeClient(ServerData data, NodeClientListener listener) {
        this.data = data;
        this.mListener = new WeakReference(listener);
        channel = ManagedChannelBuilder.forTarget(data.getTotalUrl())
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build();
        blockingStub = LeaderElectionGrpc.newBlockingStub(channel);
    }

    public void shutdownConnectionChannel() {
        channel.shutdownNow();
    }

    /**
     * This method triggers the leader election and share servers.HostServer id to other HostServers
     * @param id servers.HostServer id sent to other node in
     */
    public void electionTrigger(Integer id) {
        try {
            Runnable runnable = () -> {
                ElectionRequest request = ElectionRequest.newBuilder().setServerId(id).build();
                try {
                    ElectionResponse response = blockingStub.election(request);
                    if (mListener.get() != null) mListener.get().receveidResponseFrom(data.getTotalUrl(), response);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
    }

    /**
     *
     * @param data servers.HostServer data
     */
    public void leaderHealthTrigger(ServerData data) {
        try {
            Runnable runnable = () -> {
                LeaderHealthCheckInfo request = LeaderHealthCheckInfo
                        .newBuilder()
                        .setGrPcPort(data.getGrPcPort())
                        .setApiPort(data.getApiPort())
                        .setId(data.getId())
                        .setUrl(data.getUrl())
                        .build();
//                logger.log(Level.WARNING, "I am in leaderHealthTrigger for {0}", data.getTotalUrl());
                Empty resp = blockingStub.heartBeat(request);
            };
            Thread thread = new Thread(runnable);
            thread.start();
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
    }

    public ServerData getData() {
        return data;
    }
}
