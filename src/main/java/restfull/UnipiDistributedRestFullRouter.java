package restfull;

import static spark.Spark.*;

public class UnipiDistributedRestFullRouter {

    public void startRouting() {
        get("/hello", (req, res) -> {
            return "Hello world";
        });
        post("/auth/login", (req, res) -> {
            return "hey post";
        });
    }
}
