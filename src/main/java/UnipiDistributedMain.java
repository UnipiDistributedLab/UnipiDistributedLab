import com.google.gson.Gson;
import servers.HostServer;
import servers.lamportstorage.StorageType;
import servers.leaderelection.ServerData;
import utlis.JsonResourcesPropertiesReader;
import utlis.Utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UnipiDistributedMain {
    //default Spark port is 4567
    private static final java.util.logging.Logger logger = Logger.getLogger(UnipiDistributedMain.class.getName());
    private static HostServer hostServer;
    private static ArrayList<HostServer> hostServers = new ArrayList<>();

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        JsonResourcesPropertiesReader jsonResourcesPropertiesReader = new JsonResourcesPropertiesReader("EnvProperties.json");
        String isTestModeStr = jsonResourcesPropertiesReader.readVariable("isUnderTestMode");
        if (!Boolean.parseBoolean(isTestModeStr)) {
            startRouting(getServerData(), getAllServers("OtherNodeServers.json"));
            return;
        }
        try {
            testStartRouting(getAllServers("TestServers.json"));
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
        }
    }

    public static ServerData getServerData() {
        JsonResourcesPropertiesReader jsonResourcesPropertiesReader = new JsonResourcesPropertiesReader("NodeServer.json");
        Gson gson = new Gson();
        return gson.fromJson(jsonResourcesPropertiesReader.getJsonObject().toJSONString(), ServerData.class);
    }

    public static List<ServerData> getAllServers(String name) {
        JsonResourcesPropertiesReader jsonResourcesPropertiesReader = new JsonResourcesPropertiesReader(name);
        Gson gson = new Gson();
        ServerData[] servers = gson.fromJson(jsonResourcesPropertiesReader.readArray("servers").toJSONString(), ServerData[].class);
        return Arrays.asList(servers);
    }

    public static void startRouting(ServerData serverData, List<ServerData> otherNodesData) throws InterruptedException {
        hostServer = new HostServer(serverData, otherNodesData, serverData.getType());
    }

    public static void testStartRouting(List<ServerData> serversData) throws InterruptedException {
        for (ServerData serverData : serversData) {
            Runnable runnable = () -> {
                HostServer server = new HostServer(serverData, serversData, serverData.getType());
                //Here we start the host server for each node and also pas serversData in order to be aware for all available nodes
                hostServers.add(server);
            };
            Thread thread = new Thread(runnable);
            thread.start();
        }
        startLeaderElection();
    }

    /**
     * Or you can use start.sh
     */
    public static void startLeaderElection() {
        Thread thread = new Thread(() -> {
            URL url = null;
            try {
                Thread.sleep(3000);
                for (HostServer server : hostServers) {
                    url = new URL("http://" + server.serverData.getUrl() + ":" + server.serverData.getApiPort()+"/api/start");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setDoOutput(true);
                    con.setRequestProperty("accept", "application/json");
                    InputStream responseStream = con.getInputStream();
                    printResponse(responseStream,server);

                }
//                writeResponseTest();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
    }

    public static void printResponse(InputStream responseStream, HostServer server) {
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (responseStream, StandardCharsets.UTF_8))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.print(textBuilder + " ");
        System.out.println(server.serverData.getId());
    }

    public static void writeResponseTest() {
        for (int i = 0; i < 2; i++) {
            Thread thread = new Thread(() -> {
                URL url = null;
                try {
                    Thread.sleep(15 * 1000);
                    for (int j = 0; j < 175; j++) {
                        int serverIndex = Math.abs(Utils.getString().hashCode()) % hostServers.size();
                        servers.HostServer server = hostServers.get(serverIndex);
                        url = new URL("http://" + server.serverData.getUrl() + ":" + server.serverData.getApiPort()+"/api/write");
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("POST");
                        con.setDoOutput(true);
                        String jsonInputString = "{\"value\": \"" + Utils.getString()  + "\", \"lamportCounter\": 1}";
                        con.setRequestProperty("accept", "application/json");
                        try(OutputStream os = con.getOutputStream()) {
                            byte[] input = jsonInputString.getBytes("utf-8");
                            os.write(input, 0, input.length);
                        }
                        InputStream responseStream = con.getInputStream();
                        printResponse(responseStream,server);

                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            thread.start();
        }
    }
}
