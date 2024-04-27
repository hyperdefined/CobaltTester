package lol.hyper.cobalttester;

import org.json.JSONObject;

import java.util.Objects;

public class Instance {

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
    private int score;
    private int testsRan;

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
        instanceJSON.put("score", getScoreResults());
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


    public void setScore(int score) {
        this.score = score;
    }


    public void setTestsRan(int testsRan) {
        this.testsRan = testsRan;
    }

    public double getScoreResults() {
        if (testsRan == 0) {
            return 0.0;
        }
        return (double) score / testsRan * 100.0;
    }
}
