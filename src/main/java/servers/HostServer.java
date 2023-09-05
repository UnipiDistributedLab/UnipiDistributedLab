package servers;

import io.grpc.ServerBuilder;
import servers.controllers.MainController;
import servers.lamportstorage.LeaderStorageHelper;
import servers.lamportstorage.StorageType;
import servers.lamportstorage.ValueStorage;
import servers.leaderelection.NodeServer;
import servers.leaderelection.ServerData;
import utlis.AppStorage;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;

public final class HostServer implements NodeServer.NodeServerListener, MainController.MainControllerListener {

    public final ServerData serverData;
    final NodeServer nodeServer;
    final ServerBuilder javaServer;
    final private MainController maincontroller;
    private ArrayList<ServerData> allServersData;
    private ServerData thisServerData;
    private LeaderStorageHelper leaderStorageHelper;
    private ValueStorage valueServer;
    private TimeOutConfigParams timeOutConfigParams;

    public HostServer(ServerData serverData, ArrayList<ServerData> allServersData, StorageType type) {
        timeOutConfigParams = TimeOutConfigParams.shared();
        timeOutConfigParams.init();
        this.serverData = serverData;
        thisServerData = serverData;
        this.allServersData = allServersData;
        javaServer = ServerBuilder.forPort(serverData.getGrPcPort());
        valueServer = new ValueStorage(type, serverData);
        nodeServer = new NodeServer(serverData, allServersData, this);
        maincontroller = new MainController(this);
        maincontroller.initRestInterface(serverData.getApiPort());
        try {
            nodeServer.addElectionService(javaServer);
            valueServer.addValueService(javaServer);
            io.grpc.Server hostServer = javaServer.build().start();
            nodeServer.setServer(hostServer);
            nodeServer.setValueStorage(valueServer);
            nodeServer.blockUntilShutdown();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startPeriodicCheck() {
        nodeServer.startPeriodicCheck();
    }

    @Override
    public void leaderUpdate(@Nullable ServerData leader) {
        if (AppStorage.getInstance().getLeader() != null &&
                leader != null &&
                AppStorage.getInstance().getLeader().getId() != leader.getId()) {
            ArrayList<ServerData> udpatedServers = new ArrayList<>();
            for (ServerData serverInst : allServersData) {
                if (serverInst.getId() <= leader.getId()) udpatedServers.add(serverInst);
            }
            allServersData = udpatedServers;
            if (leaderStorageHelper != null) leaderStorageHelper.updateAllServer(udpatedServers);
        }
        AppStorage.getInstance().setLeader(leader);
        maincontroller.leaderIs(leader);
    }

    @Override
    public void stopStorageServer(ServerData leader) {
        ArrayList<ServerData> udpatedServers = new ArrayList<>();
        for (ServerData serverInst : allServersData) {
            if (serverInst.getId() <= leader.getId()) udpatedServers.add(serverInst);
        }
        allServersData = udpatedServers;
        leaderStorageHelper = new LeaderStorageHelper(allServersData, thisServerData, javaServer);
        valueServer.setLeader(leaderStorageHelper);
    }

    @Override
    public void leaderKilled() {
        if (valueServer == null) return;
        valueServer.stopOperations();
        valueServer = null;
    }
}
