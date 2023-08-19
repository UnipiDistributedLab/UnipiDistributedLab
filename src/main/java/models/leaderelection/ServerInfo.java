package models.leaderelection;

public final class ServerInfo {

    private int port;
    private int id;
    private String ip;

    public ServerInfo(int port, int id, String ip) {
        this.port = port;
        this.id = id;
        this.ip = ip;
    }

    @Override
    public int hashCode() {
        return getUrl().hashCode();
    }

    public int getPort() {
        return port;
    }

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public String getUrl() {
        return ip + ":" + port;
    }
}
