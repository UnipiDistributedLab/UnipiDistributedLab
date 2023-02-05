package servers;

import jdk.jfr.internal.LogLevel;
import servers.rest.StoreValueController;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static spark.Spark.get;
import static spark.Spark.post;

public class MainValueStore {
    //default Spark port is 4567
    private static final Logger logger = Logger.getLogger(MainValueStore.class.getName());
    private static final StoreValueController storeController = new StoreValueController();


    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            storeController.startRouting();
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
        }
    }
}