package servers.rest.response;

import javax.annotation.Nullable;

public class WriteValueResponse {
    @Nullable
    Integer lamportCounter;

    public WriteValueResponse(Integer lamportCounter) {
        this.lamportCounter = lamportCounter;
    }
}
