package rest;

import conformers.RoutingControllerConformer;

import java.util.logging.Level;
import java.util.logging.Logger;

import static spark.Spark.get;

public class UnipiDistributedRestRouter implements RoutingControllerConformer {
    //default Spark port is 4567
    @Override
    public void startRouting() {
        get("/hello", (req, res) -> {
            return "Hello world";
        });
    }
    }

