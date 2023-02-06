import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.unipi.election.*;
import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NodeClient {

    private static final Logger logger = Logger.getLogger(NodeClient.class.getName());
    private final LeaderElectionGrpc.LeaderElectionBlockingStub blockingStub;
    private String target;
    private WeakReference<NodeClientListener> mListener;

    public NodeClient(String target, NodeClientListener listener) {
        this.target = target;
        this.mListener = new WeakReference(listener);
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build();
        blockingStub = LeaderElectionGrpc.newBlockingStub(channel);
    }

    public void electionTrigger(Integer id) {
        try {
            Runnable runnable = () -> {
                ElectionRequest request = ElectionRequest.newBuilder().setServerId(id).build();
                ElectionResponse response = blockingStub.election(request);
                if (mListener.get() != null) mListener.get().receveidResponseFrom(target, response);
                logger.info("Response tagret" + target + " : " + response.hasOkMessage());
            };
            ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
            scheduler.schedule(runnable, 0, TimeUnit.SECONDS);

        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
    }

    public void leaderHealthTrigger(ServerData data) {
        try {
            Runnable runnable = () -> {
                LeaderHealthCheckInfo request = LeaderHealthCheckInfo
                        .newBuilder()
                        .setPort(data.getPort())
                        .setId(data.getId())
                        .setUrl(data.getUrl())
                        .build();
                Empty _ = blockingStub.leaderHealthCheck(request);
            };
            ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
            scheduler.schedule(runnable, 0, TimeUnit.SECONDS);

        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
    }

    public interface NodeClientListener {
        void receveidResponseFrom(String targer, ElectionResponse response);
    }
}
