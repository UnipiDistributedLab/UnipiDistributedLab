import restfull.UnipiDistributedRestFullRouter;
import rpc.UnipiDistributedRpcRouter;

public class UnipiDistributedMain {
    public static void main(String[] args) {
        UnipiDistributedRestFullRouter restRouter = new UnipiDistributedRestFullRouter();
        restRouter.startRouting();

        UnipiDistributedRpcRouter rpcRouter = new UnipiDistributedRpcRouter();
        rpcRouter.startRouting();
    }
}
