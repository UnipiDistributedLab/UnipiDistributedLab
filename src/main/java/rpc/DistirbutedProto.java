package rpc;

import io.grpc.stub.StreamObserver;
import io.grpc.unipi.distributed.APIResponse;
import io.grpc.unipi.distributed.DistributeAuthenticationGrpc;

public class DistirbutedProto extends DistributeAuthenticationGrpc.DistributeAuthenticationImplBase {
    @Override
    public void hello(io.grpc.unipi.distributed.UserName request, StreamObserver<APIResponse> responseObserver) {
        super.hello(request, responseObserver);
    }
}
