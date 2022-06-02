import io.grpc.Server;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ServerController {
    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        List<Integer> serversIds = Arrays.asList(0,1,2,3,4,5,6,7);
        for (Integer serverId : serversIds) {
            final NodeServer server = new NodeServer(serverId);
            Server serverInst = server.start();
            server.blockUntilShutdown();
        }
    }
}
