package restfull;

import conformers.RoutingControllerConformer;
import restfull.controllers.login.LoginController;

import static spark.Spark.*;

public class UnipiDistributedRestFullRouter implements RoutingControllerConformer {

    public void startRouting() {
        LoginController loginController = new LoginController();
        loginController.startRouting();
    }
}
