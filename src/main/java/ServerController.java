import io.grpc.Server;
import io.grpc.unipi.election.ElectionRequest;
import io.grpc.unipi.election.ElectionResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerController {

    public static List<Integer> serversIds = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    private static final Integer defaultPort = 50051;

    public static void main(String[] args) throws IOException, InterruptedException {
        ArrayList<ServerData> mappedServersData = new ArrayList<>(serversIds.size());
        for (Integer serverId : serversIds) {
            Integer port = defaultPort + serverId;
            mappedServersData.add(new ServerData(port, serverId));
        }
        for (Integer serverId : serversIds) {
            Runnable runnable = () -> {

                Integer port = defaultPort + serverId;
                ServerData newServerData = new ServerData(port, serverId);
                final NodeServer server = new NodeServer(newServerData, mappedServersData);
                try {
                    Server serverInst = server.start();
                    server.blockUntilShutdown();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            };
            ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
            scheduler.schedule(runnable, 0, TimeUnit.SECONDS);
        }
    }
}
