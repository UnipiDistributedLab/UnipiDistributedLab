package servers.rest.request;

public class WriteValueResponse {
    int lamportCounter;

    public WriteValueResponse(int lamportCounter) {
        this.lamportCounter = lamportCounter;
    }
}
