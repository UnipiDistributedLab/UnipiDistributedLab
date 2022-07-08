package restfull.controllers.login;

import com.google.gson.Gson;
import conformers.RoutingControllerConformer;

import static spark.Spark.*;

public class LoginController implements RoutingControllerConformer {

    private static final Gson mGson = new Gson();

    public void startRouting() {
        post("/auth/login", (req, res) -> {
            LoginAuthRequest test = mGson.fromJson(req.body(), LoginAuthRequest.class);
            return "hey post";
        });
        get("/hello", (req, res) -> {
            return "hello";
        });
    }
}

