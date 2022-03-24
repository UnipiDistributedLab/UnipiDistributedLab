import static spark.Spark.*;

public class UnipiDistributedMain {
    public static void main(String[] args) {
        get("/hello", (req, res) -> {
            return "Hello world";
        });
        post("/auth/login", (req, res) -> {
          return "hey post";
        });
    }
}
