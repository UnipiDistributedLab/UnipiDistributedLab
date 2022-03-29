package rpc;

import io.grpc.stub.StreamObserver;
import io.grpc.unipi.distributed.APIResponse;
import io.grpc.unipi.distributed.DistributeAuthenticationGrpc;
import io.grpc.unipi.distributed.UserName;

public class AuthLoginImpl extends DistributeAuthenticationGrpc.DistributeAuthenticationImplBase {
    @Override
    public void doAuthLogin(UserName request, StreamObserver<APIResponse> responseObserver) {
        super.doAuthLogin(request, responseObserver);
    }
}
