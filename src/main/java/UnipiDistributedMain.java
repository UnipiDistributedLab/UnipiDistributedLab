import restfull.UnipiDistributedRestFullRouter;

import static spark.Spark.port;

public class UnipiDistributedMain {

    // Default url: http://localhost:4567
    public static void main(String[] args) {
        UnipiDistributedRestFullRouter restRouter = new UnipiDistributedRestFullRouter();
        restRouter.startRouting();
    }
}
