public class ServerData {

    private int port;
    private int id;
    private String url;

    public ServerData(int port, int id, String url) {
        this.port = port;
        this.id = id;
        this.url = url;
    }

    public int getPort() {
        return port;
    }

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }
}
