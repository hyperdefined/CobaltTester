package lol.hyper.cobalttester.instance;

import lol.hyper.cobalttester.requests.RequestUtil;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class Instance implements Comparable<Instance> {

    private final String frontEnd;
    private final String api;
    private final String protocol;
    private String trustStatus;
    private String version;
    private String commit;
    private String branch;
    private String name;
    private int cors;
    private long startTime;
    private boolean apiWorking;
    private double score;
    private String hash;
    private final Logger logger = LogManager.getLogger(this);

    private final Map<String, Boolean> testResults = new TreeMap<>();

    public Instance(String frontEnd, String api, String protocol, String trustStatus) {
        this.frontEnd = frontEnd;
        this.api = api;
        this.protocol = protocol;
        this.trustStatus = trustStatus;
    }

    public JSONObject toJSON() {
        JSONObject instanceJSON = new JSONObject();
        instanceJSON.put("version", this.version);
        instanceJSON.put("commit", this.commit);
        instanceJSON.put("branch", this.branch);
        instanceJSON.put("name", this.name);
        instanceJSON.put("api", this.api);
        instanceJSON.put("cors", this.cors);
        instanceJSON.put("startTime", Long.valueOf(this.startTime));
        instanceJSON.put("api_online", this.apiWorking);
        instanceJSON.put("frontEnd", Objects.requireNonNullElse(frontEnd, "None"));
        instanceJSON.put("protocol", protocol);
        instanceJSON.put("score", score);
        instanceJSON.put("trust", trustStatus);
        JSONObject workingServices = new JSONObject();
        for (Map.Entry<String, Boolean> pair : testResults.entrySet()) {
            String service = pair.getKey().toLowerCase(Locale.ROOT).replace(" ", "_");
            // skip frontend here
            if (service.equalsIgnoreCase("Frontend")) {
                continue;
            }
            boolean working = pair.getValue();
            workingServices.put(service, working);
        }
        instanceJSON.put("services", workingServices);
        return instanceJSON;
    }

    public String toString() {
        return this.api;
    }

    public String getName() {
        return name;
    }

    public int getCors() {
        return cors;
    }

    public String getApi() {
        return api;
    }

    public String getBranch() {
        return branch;
    }

    public String getCommit() {
        return commit;
    }

    public String getFrontEnd() {
        return frontEnd;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getVersion() {
        return version;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public void setCors(int cors) {
        this.cors = cors;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isApiWorking() {
        return apiWorking;
    }

    public void setApiWorking(boolean apiWorking) {
        this.apiWorking = apiWorking;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    public Map<String, Boolean> getTestResults() {
        return testResults;
    }

    public void addResult(String service, boolean working) {
        testResults.put(service, working);
    }

    public void addCurve(int curve) {
        score = score + curve;
    }

    public String getTrustStatus() {
        return trustStatus;
    }

    public void setTrustStatus(String trustStatus) {
        this.trustStatus = trustStatus;
    }

    public void calculateScore() {
        long workingServices = testResults.values().stream().filter(Boolean::booleanValue).count();
        int totalTestsRan = testResults.size();
        if (totalTestsRan > 0) {
            score = ((double) workingServices / totalTestsRan) * 100;
        } else {
            score = 0;
        }
    }

    /**
     * Save the API's information.
     */
    public void loadApiJSON() {
        String url = protocol + "://" + api + "/api/serverInfo";
        logger.info("Reading API information for {}", url);
        JSONObject apiJson = RequestUtil.requestJSON(url);
        if (apiJson == null) {
            this.setApiWorking(false);
            this.setTrustStatus("offline");
            this.setName("Offline");
            this.setCommit("Offline");
            this.setBranch("Offline");
            this.setName("Offline");
            this.setVersion("Offline");
            return;
        }
        this.setApiWorking(true);
        if (apiJson.has("name")) {
            String name = apiJson.getString("name");
            this.setName(StringEscapeUtils.escapeHtml4(name));
        }
        if (apiJson.has("version")) {
            String version = apiJson.getString("version");
            // older instances had -dev in the version
            if (version.contains("-dev")) {
                version = version.replace("-dev", "");
            }
            this.setVersion(StringEscapeUtils.escapeHtml4(version));
        }
        if (apiJson.has("commit")) {
            String commit = apiJson.getString("commit");
            this.setCommit(StringEscapeUtils.escapeHtml4(commit));
        }
        if (apiJson.has("branch")) {
            String branch = apiJson.getString("branch");
            this.setBranch(StringEscapeUtils.escapeHtml4(branch));
        }
        if (apiJson.has("cors")) {
            int cors = apiJson.getInt("cors");
            if (cors == 0 || cors == 1) {
                this.setCors(cors);
            } else {
                this.setCors(-1);
                logger.warn("{} has an invalid cors!", this.getApi());
            }
        }
        if (apiJson.has("startTime")) {
            String startTimeString = String.valueOf(apiJson.getLong("startTime"));
            if (startTimeString.matches("[0-9]+")) {
                this.setStartTime(apiJson.getLong("startTime"));
            } else {
                this.setStartTime(0L);
                logger.warn("{} has an invalid startTime!", this.getApi());
            }
        }
    }

    @Override
    public int compareTo(Instance instance) {
        return this.api.compareTo(instance.api);
    }
}
