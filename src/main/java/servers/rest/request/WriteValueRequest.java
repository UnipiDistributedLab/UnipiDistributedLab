package servers.rest.request;

public class WriteValueRequest {
    String value;
    int lamportCounter;

    public String getValue() {
        return value;
    }

    public int getLamportCounter() {
        return lamportCounter;
    }
}
