import com.google.gson.Gson;
import conformers.RoutingControllerConformer;
import helpers.LoginHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rest.LoginController;
import rest.UnipiDistributedRestRouter;
import rpc.UnipiDistributedRpcRouter;

public class UnipiDistributedMain {
    public static void main(String[] args) {
        Gson gson = new Gson();
        UnipiDistributedRestRouter restRouter =  new UnipiDistributedRestRouter();
        restRouter.startRouting();
        UnipiDistributedRpcRouter rpcRouter = new UnipiDistributedRpcRouter();
        rpcRouter.startRouting();
        LoginHelper helper = new LoginHelper(gson);
        RoutingControllerConformer loginRest = new LoginController(gson, helper);
        loginRest.startRouting();
    }
}
