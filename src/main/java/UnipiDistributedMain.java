import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rest.UnipiDistributedRestRouter;
import rpc.UnipiDistributedRpcRouter;

public class UnipiDistributedMain {
    public static void main(String[] args) {
        UnipiDistributedRestRouter restRouter =  new UnipiDistributedRestRouter();
        restRouter.startRouting();
        UnipiDistributedRpcRouter rpcRouter = new UnipiDistributedRpcRouter();
        rpcRouter.startRouting();
    }
}
