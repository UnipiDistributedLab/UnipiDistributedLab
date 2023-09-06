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
    private Integer archiveLogsCycle;
    private Integer clearLogFileCycle;
    private Boolean supportsClearLogFileCycle;

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
            archiveLogsCycle = Integer.parseInt(jsonObject.get("archiveLogsCycle").toString());
            clearLogFileCycle = Integer.parseInt(jsonObject.get("clearLogFileCycle").toString());
            supportsClearLogFileCycle = Boolean.parseBoolean(jsonObject.get("supportsClearLogFileCycle").toString());
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

    public Integer getArchiveLogsCycle() {
        return archiveLogsCycle;
    }

    public Integer getClearLogFileCycle() {
        return clearLogFileCycle;
    }

    public Boolean getSupportsClearLogFileCycle() {
        return supportsClearLogFileCycle;
    }
}
