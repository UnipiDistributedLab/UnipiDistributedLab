package rest;

import conformers.RoutingControllerConformer;

import static spark.Spark.get;

public class UnipiDistributedRestRouter implements RoutingControllerConformer {

    @Override
    public void startRouting() {
        get("/hello", (req, res) -> {
            return "Hello world";
        });
    }
    }

