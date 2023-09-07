package servers.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.unipi.election.LeaderElectionGrpc;
import io.grpc.unipi.election.LeaderKillRequest;
import servers.leaderelection.ServerData;
import servers.rest.request.ReadValueResponse;
import servers.rest.request.WriteValueRequest;
import servers.rest.response.GenericResponse;
import servers.rest.response.KillLeaderResponse;
import servers.rest.response.LeaderResponse;
import servers.rest.response.WriteValueResponse;
import spark.Service;
import utlis.AppStorage;
import utlis.Utils;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.util.logging.Logger;

import static spark.Service.ignite;

public class MainController {

    public interface MainControllerListener {
        void startPeriodicCheck();
    }
    private final Logger logger = Logger.getLogger(MainController.class.getName());
    private static final Gson mGson = new Gson();
    private @Nullable ManagedChannel leaderChannel;
    private ServerData leader;
    private WeakReference<MainControllerListener> mListener;

    public MainController(MainControllerListener listener) {
        mListener = new WeakReference<>(listener);
    }

    //Spark default port is 4567
    public void initRestInterface(int port) {
        Service http = ignite()
                .port(port);
        http.get("api/start", (req, res) -> {
            if (mListener.get() != null) mListener.get().startPeriodicCheck();
            return mGson.toJson(new GenericResponse("Success start", true));
        });
        http.get("api/health", (req, res) -> {
            JsonObject json = new JsonObject();
            json.addProperty("uniTIme", System.currentTimeMillis());
            return json;
        });
        http.get("api/ping", (req, res) -> {
            return "pong";
        });
        http.post("api/killleader", (req, res) -> {
            try {
                ServerData leaderData = AppStorage.getInstance().getLeader();
                LeaderKillRequest request = LeaderKillRequest.newBuilder().setLeaderId(leaderData.getId()).build();
                LeaderElectionGrpc.LeaderElectionBlockingStub blockingStub = LeaderElectionGrpc.newBlockingStub(leaderChannel);
                blockingStub.leaderKill(request);
//                AppStorage.getInstance().getStub().leaderKill(request).get();
            } catch (Exception e) {
                logger.warning(e.getMessage());
                return mGson.toJson(new KillLeaderResponse(e.getMessage())) ;
            } finally {
                return mGson.toJson(new KillLeaderResponse("Leader killed")) ;
            }
        });
        http.get("api/getleader", (req, res) -> {
            if (AppStorage.getInstance().getLeader() == null) {
                return mGson.toJson(new LeaderResponse("Election not finished", null));
            }
            return mGson.toJson(new LeaderResponse("success", AppStorage.getInstance().getLeader()));
        });
    }

    private void initLeaderRPC(@Nullable ServerData leader) throws InterruptedException {
        if (this.leader != null) {
            if (leader != null && this.leader.getId() == leader.getId()) return;
        }
        this.leader = leader;
        if (leaderChannel != null) leaderChannel.shutdownNow();
        leaderChannel =  ManagedChannelBuilder.forTarget(leader.getTotalUrl())
                .usePlaintext()
                .build();
    }

    public void leaderIs(@Nullable ServerData leader) {
        if (leader == null) return;
        try {
            this.initLeaderRPC(leader);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
