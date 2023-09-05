package servers.rest.response;

import servers.leaderelection.ServerData;

import javax.annotation.Nullable;

public class LeaderResponse {
    String message;
    @Nullable ServerData server;

    public LeaderResponse(String message, @Nullable ServerData server) {
        this.message = message;
        this.server = server;
    }
}
