package rpc;

import io.grpc.stub.StreamObserver;
import io.grpc.unipi.distributed.*;

public class AuthLoginImpl extends DistributeAuthenticationGrpc.DistributeAuthenticationImplBase {
    @Override
    public void doAuthLogin(UserName request, StreamObserver<APIResponse> responseObserver) {
        System.out.println(request.getPassWord());
        // DB.connect.getUserData(request.getUserName);
//        super.doAuthLogin(request, responseObserver);
        WalletModel walletModel = WalletModel
                .newBuilder()
                .setBalance(10.0F)
                .setFreeCoin(1000).build();
        io.grpc.unipi.distributed.UserModel userModel = UserModel
                .newBuilder()
                .setUserName("Distributes")
                .setEmail("distributed@unipi.gr")
                .setWallet(walletModel)
                .build();
        APIResponse apiResponse = APIResponse
                .newBuilder()
                .setUserModel(userModel)
                .setMessageCode(200)
                .setResponseMessage("Success login")
                .build();
        responseObserver.onNext(apiResponse);
        responseObserver.onCompleted();
    }
}
