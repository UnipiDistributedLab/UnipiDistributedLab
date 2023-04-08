package rest;

import com.google.gson.Gson;
import conformers.RoutingControllerConformer;
import helpers.LoginHelper;
import models.UserModel;

import static spark.Spark.post;

public class LoginController implements RoutingControllerConformer {
    private Gson gson;
    private LoginHelper loginHelper;
    public LoginController(Gson gson, LoginHelper loginHelper) {
        this.gson = gson;
        this.loginHelper = loginHelper;
    }
    public void startRouting() {
        post("/login", (req, res) -> {
            UserModel userModel = loginHelper.handle(req.body());
            return "Hello " + userModel.userName;
        });
    }
}
