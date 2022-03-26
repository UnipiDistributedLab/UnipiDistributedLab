package rpc;

import io.grpc.stub.StreamObserver;
import io.grpc.unipi.distributed.APIResponse;
import io.grpc.unipi.distributed.DistributeAuthenticationGrpc;
import io.grpc.unipi.distributed.UserName;

public class DistirbutedProto extends DistributeAuthenticationGrpc.DistributeAuthenticationImplBase {
    @Override
    public void authLogin(UserName request, StreamObserver<APIResponse> responseObserver) {
        super.authLogin(request, responseObserver);
    }
}
