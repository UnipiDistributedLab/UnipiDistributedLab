
import static spark.Spark.get;

public class UnipiDistributedMain {
    public static void main(String[] args) {
        get("/hello", (req, res) -> {
            return "Hello world";
        });
    }
}
