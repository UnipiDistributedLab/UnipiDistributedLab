package servers;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

public class TimeOutConfigParams {

    private static TimeOutConfigParams instance;
    private Long leaderHeartBeatPeriodMS;
    private Long serverShutdownTimeoutMs;
    private Integer electionTimeOutPeriodMS;
    private Integer archiveLogsCycle;
    private Integer clearLogFileCycle;
    private Integer electionFinishTimeOutMinPeriodMS;
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
            Random rand = new Random();
            leaderHeartBeatPeriodMS = (Long) jsonObject.get("leaderHeartBeatPeriodMS");
            serverShutdownTimeoutMs = (Long) jsonObject.get("serverShutdownTimeoutMs");
            archiveLogsCycle = Integer.parseInt(jsonObject.get("archiveLogsCycle").toString());
            clearLogFileCycle = Integer.parseInt(jsonObject.get("clearLogFileCycle").toString());
            supportsClearLogFileCycle = Boolean.parseBoolean(jsonObject.get("supportsClearLogFileCycle").toString());
            electionFinishTimeOutMinPeriodMS = Integer.parseInt(jsonObject.get("electionFinishTimeOutMinPeriodMS").toString());
            int electionTimeOutMaxPeriodMS = Integer.parseInt(jsonObject.get("electionTimeOutMaxPeriodMS").toString());
            int electionTimeOutMinPeriodMS = Integer.parseInt(jsonObject.get("electionTimeOutMinPeriodMS").toString());
            electionTimeOutPeriodMS = rand.nextInt(electionTimeOutMaxPeriodMS - electionTimeOutMinPeriodMS) + electionTimeOutMinPeriodMS;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Long getLeaderHeartBeatPeriodMS() {
        return leaderHeartBeatPeriodMS;
    }

    public Long getServerShutdownTimeoutMs() {
        return serverShutdownTimeoutMs;
    }

    public Integer getElectionTimeOutPeriodMS() {
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

    public Integer getElectionFinishTimeOutMinPeriodMS() {
        return electionFinishTimeOutMinPeriodMS;
    }
}
