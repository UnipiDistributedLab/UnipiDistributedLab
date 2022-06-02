import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.unipi.election.ElectionRequest;
import io.grpc.unipi.election.ElectionResponse;
import io.grpc.unipi.election.LeaderElectionGrpc;

import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NodeClient {

    private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());
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

    /** Say hello to server. */
    public void electionTrigger(Integer id) {
        ElectionRequest request = ElectionRequest.newBuilder().setServerId(id).build();
        try {
            ElectionResponse response = blockingStub.election(request);
            response.hasOkMessage();
            if (mListener.get() != null) mListener.get().receveidResponseFrom(target, response);
            logger.info("Greeting tagret" + target + " : " + response.hasOkMessage());
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
    }

    public interface NodeClientListener {
        void receveidResponseFrom(String targer, ElectionResponse response);
    }
}
