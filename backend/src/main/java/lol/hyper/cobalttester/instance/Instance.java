package lol.hyper.cobalttester.instance;

import org.json.JSONObject;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class Instance implements Comparable<Instance> {

    private final String frontEnd;
    private final String api;
    private final String protocol;
    private String version;
    private String commit;
    private String branch;
    private String name;
    private int cors;
    private long startTime;
    private boolean apiWorking;
    private boolean frontEndWorking;
    private double score;
    private String hash;

    private final Map<String, Boolean> testResults = new TreeMap<>();

    public Instance(String frontEnd, String api, String protocol) {
        this.frontEnd = frontEnd;
        this.api = api;
        this.protocol = protocol;
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
        instanceJSON.put("frontend_online", this.frontEndWorking);
        instanceJSON.put("frontEnd", Objects.requireNonNullElse(frontEnd, "None"));
        instanceJSON.put("protocol", protocol);
        instanceJSON.put("score", score);
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

    public boolean isFrontEndWorking() {
        return frontEndWorking;
    }

    public void setApiWorking(boolean apiWorking) {
        this.apiWorking = apiWorking;
    }

    public void setFrontEndWorking(boolean frontEndWorking) {
        this.frontEndWorking = frontEndWorking;
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

    @Override
    public int compareTo(Instance instance) {
        return this.api.compareTo(instance.api);
    }
}
