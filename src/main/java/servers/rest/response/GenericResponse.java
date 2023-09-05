package servers.rest.response;

public class GenericResponse {
    String message;
    Boolean success;

    public GenericResponse(String message, Boolean success) {
        this.message = message;
        this.success = success;
    }
}
