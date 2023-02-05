package servers;

import servers.rest.StoreValueController;

import java.io.IOException;
import java.util.logging.Logger;

import static spark.Spark.get;
import static spark.Spark.post;

public class ReplicatedValueStore {
    //default Spark port is 4567
    private static final Logger logger = Logger.getLogger(ReplicatedValueStore.class.getName());
    private static StoreValueController storeController = new StoreValueController();

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        storeController.startRouting();
    }
}