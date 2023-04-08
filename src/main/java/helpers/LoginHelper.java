package helpers;

import com.google.gson.Gson;
import models.UserModel;

public class LoginHelper {
    private Gson gson;
    public LoginHelper(Gson gson) {
        this.gson = gson;
    }
    public UserModel handle(String body) {
        return  gson.fromJson(body, UserModel.class);
    }
}
