package rpc;

import io.grpc.stub.StreamObserver;
import io.grpc.unipi.distributed.APIResponse;
import io.grpc.unipi.distributed.DistributeAuthenticationGrpc;
import io.grpc.unipi.distributed.Empty;

public class DistirbutedProto extends DistributeAuthenticationGrpc.DistributeAuthenticationImplBase {
    @Override
    public void hello(Empty request, StreamObserver<APIResponse> responseObserver) {
        super.hello(request, responseObserver);
    }
}
