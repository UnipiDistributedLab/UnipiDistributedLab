import restfull.UnipiDistributedRestFullRouter;

public class UnipiDistributedMain {
    public static void main(String[] args) {
        UnipiDistributedRestFullRouter restRouter = new UnipiDistributedRestFullRouter();
        restRouter.startRouting();
    }
}
