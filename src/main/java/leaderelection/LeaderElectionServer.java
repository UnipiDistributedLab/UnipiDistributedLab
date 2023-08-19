package leaderelection;

import io.grpc.Server;
import io.grpc.examples.servers.BullyElection.*;
import io.grpc.stub.StreamObserver;
import models.leaderelection.ServerInfo;

import java.lang.ref.WeakReference;
import java.util.logging.Logger;

public class LeaderElectionServer extends LeaderElectionGrpc.LeaderElectionImplBase {

    interface LeaderElectionServerListener {
        void leaderHealthCheck(LeaderHealthCheckInfo request);
    }

    private static final Logger logger = Logger.getLogger(LeaderElectionServer.class.getName());
    private ServerInfo server;
    private WeakReference<LeaderElectionServerListener> mListener;

    public LeaderElectionServer(ServerInfo server, LeaderElectionServerListener listener) {
        this.mListener = new WeakReference<>(listener);
        this.server = server;
    }

    @Override
    public void election(ElectionRequest request, StreamObserver<ElectionResponse> responseObserver) {
        boolean biggerId = server.getId() > request.getId();
        ElectionResponse electionResponse = ElectionResponse.newBuilder().setOk(biggerId).build();
        responseObserver.onNext(electionResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void leaderHealthCheck(LeaderHealthCheckInfo request, StreamObserver<NodeHealthCheckResponseInfo> responseObserver) {
        logger.info(" I " + server.getUrl() + " received LEADER " + request.getNodeIP());
        mListener.get().leaderHealthCheck(request);
        NodeHealthCheckResponseInfo response = NodeHealthCheckResponseInfo
                .newBuilder()
                .setNodeIP(server.getUrl())
                .setId(server.getId())
                .setPort(server.getPort())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void giveMeTheLeader(Empty request, StreamObserver<LeaderHealthCheckInfo> responseObserver) {
        super.giveMeTheLeader(request, responseObserver);
    }
}