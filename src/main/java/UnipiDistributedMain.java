import rpc.UnipiDistributedRpcRouter;

public class UnipiDistributedMain {
    public static void main(String[] args) {

        UnipiDistributedRpcRouter rpcRouter = new UnipiDistributedRpcRouter();
        rpcRouter.startRouting();
    }
}
