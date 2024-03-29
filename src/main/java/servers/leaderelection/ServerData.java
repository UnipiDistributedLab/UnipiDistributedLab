package servers.leaderelection;

import servers.lamportstorage.StorageType;

public class ServerData extends Object {

    private int grPcPort;
    private int apiPort;
    private int id;
    private String url;
    private StorageType type;

    public ServerData(int grPcPort, int apiPort, int id, String url, StorageType type) {
        this.grPcPort = grPcPort;
        this.apiPort = apiPort;
        this.id = id;
        this.url = url;
        this.type = type;
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

    public io.grpc.unipi.election.StorageType getGrpcType() {
        return io.grpc.unipi.election.StorageType.forNumber(type.getValue());
    }

    public StorageType getType() {
        return type;
    }

    public void setType(StorageType type) {
        this.type = type;
    }
}
