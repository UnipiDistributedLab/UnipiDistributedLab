package utlis;

import io.grpc.unipi.election.LeaderElectionGrpc;
import servers.leaderelection.ServerData;

import javax.annotation.Nullable;

public final class AppStorage {

    private Atomic<ServerData> leaderCache = new Atomic<>();

    private  Atomic<LeaderElectionGrpc.LeaderElectionFutureStub> stubCache = new Atomic<>();

    private static AppStorage instance;
    public static AppStorage getInstance() {
        if (instance == null) {
            instance = new AppStorage();
        }
        return instance;
    }

    public @Nullable ServerData getLeader() {
        return leaderCache.get();
    }

    public void setLeader(@Nullable ServerData leader) {
        if (leader == null) {
            this.leaderCache.setNull();
            return;
        }
        this.leaderCache.set(leader);
    }

    public LeaderElectionGrpc.LeaderElectionFutureStub getStub() {
        return stubCache.get();
    }

    public void setStub(LeaderElectionGrpc.LeaderElectionFutureStub stub) {
        this.stubCache.set(stub);
    }
}
