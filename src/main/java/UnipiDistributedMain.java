import restfull.UnipiDistributedRestFullRouter;
import rpc.UnipiDistributedRpcRouter;

import static spark.Spark.*;

public class UnipiDistributedMain {
    public static void main(String[] args) {
        UnipiDistributedRestFullRouter restRouter = new UnipiDistributedRestFullRouter();
        restRouter.startRouting();

        UnipiDistributedRpcRouter rpcRouter = new UnipiDistributedRpcRouter();
        restRouter.startRouting();
    }
}
