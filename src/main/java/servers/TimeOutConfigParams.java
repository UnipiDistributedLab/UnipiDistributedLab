package servers;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.io.InputStreamReader;

public class TimeOutConfigParams {

    private static TimeOutConfigParams instance;

    private Long valueStorageSyncPeriodMS;
    private Long leaderHeartBeatPeriodMS;
    private Long serverShutdownTimeoutMs;
    private Long electionTimeOutPeriodMS;

    public static TimeOutConfigParams shared() {
        if (instance == null) {
            instance = new TimeOutConfigParams();
        }
        return instance;
    }

    public void init() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("TimersConfig.json");
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
            valueStorageSyncPeriodMS = (Long) jsonObject.get("valueStorageSyncPeriodMS");
            leaderHeartBeatPeriodMS = (Long) jsonObject.get("leaderHeartBeatPeriodMS");
            serverShutdownTimeoutMs = (Long) jsonObject.get("serverShutdownTimeoutMs");
            electionTimeOutPeriodMS = (Long) jsonObject.get("electionTimeOutPeriodMS");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Long getValueStorageSyncPeriodMS() {
        return valueStorageSyncPeriodMS;
    }

    public Long getLeaderHeartBeatPeriodMS() {
        return leaderHeartBeatPeriodMS;
    }

    public Long getServerShutdownTimeoutMs() {
        return serverShutdownTimeoutMs;
    }

    public Long getElectionTimeOutPeriodMS() {
        return electionTimeOutPeriodMS;
    }
}
