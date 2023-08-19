import com.google.gson.Gson;
import conformers.RoutingControllerConformer;
import helpers.LoginHelper;
import leaderelection.NodeServer;
import models.leaderelection.ServerInfo;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rest.LoginController;
import rest.NotesController;
import rest.UnipiDistributedRestRouter;
import rpc.UnipiDistributedRpcRouter;

import java.util.*;

public class UnipiDistributedMain {
    public static void main(String[] args) {
        Gson gson = new Gson();
        RoutingControllerConformer notesController = new NotesController(gson);
        notesController.startRouting();
        UnipiDistributedRestRouter restRouter =  new UnipiDistributedRestRouter();
        restRouter.startRouting();
        UnipiDistributedRpcRouter rpcRouter = new UnipiDistributedRpcRouter();
        rpcRouter.startRouting();
        LoginHelper helper = new LoginHelper(gson);
        RoutingControllerConformer loginRest = new LoginController(gson, helper);
        loginRest.startRouting();

        // Start leader elections servers
        List<Integer> servers = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        ArrayList<ServerInfo> allServers = new ArrayList<>();
        ArrayList<NodeServer> nodeServers = new ArrayList<>();
        for (int serverId : servers) {
            ServerInfo server = new ServerInfo(9000 + serverId, serverId, "localhost");
            allServers.add(server);
            NodeServer nodeServer = new NodeServer(server);
            nodeServers.add(nodeServer);
            nodeServer.startRouting();
        }

        // Add all clients to servers
        for (NodeServer nodeServer : nodeServers) {
            for (ServerInfo serverInfo: allServers) {
              if (!serverInfo.getUrl().equals(nodeServer.getServerInfo().getUrl())) nodeServer.addOtherServer(serverInfo);
            }
            nodeServer.startPeriodicLeaderCheck();
        }

//        nodeServers.get(0).startElection();
//
//        // Prosthesa ton teleutaio kai enan endiameso gia na sugkrinetai ta apotelesmata sta logs
//        nodeServers.get(2).startElection();
//        nodeServers.get(5).startElection();
    }
}
