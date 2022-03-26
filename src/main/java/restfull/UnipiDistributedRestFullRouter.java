package restfull;

import com.google.gson.Gson;
import conformers.RoutingControllerConformer;
import restfull.controllers.LoginController;

import static spark.Spark.*;

public class UnipiDistributedRestFullRouter implements RoutingControllerConformer {

    public void startRouting() {
        get("/hello", (req, res) -> {
            return "Hello world";
        });
        LoginController loginController = new LoginController();
        loginController.startRouting();
    }
}
