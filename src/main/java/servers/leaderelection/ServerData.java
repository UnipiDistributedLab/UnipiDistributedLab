package servers.leaderelection;

public class ServerData extends Object {

    private int grPcPort;
    private int apiPort;
    private int id;
    private String url;

    public ServerData(int grPcPort, int apiPort, int id, String url) {
        this.grPcPort = grPcPort;
        this.apiPort = apiPort;
        this.id = id;
        this.url = url;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof  ServerData)) return false;
        return ((ServerData) obj).id == this.id;
    }

    public int getGrPcPort() {
        return grPcPort;
    }

    public int getApiPort() {
        return apiPort;
    }

    public int getId() {
        return id;
    }

    public String getTotalUrl() {
        return url + ":" + grPcPort;
    }

    public String getUrl() {
        return url;
    }
}
