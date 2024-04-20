package lol.hyper.cobalttester;

import org.json.JSONObject;

import java.util.Objects;

public class Instance {

    private final String frontEnd;
    private final String version;
    private final String commit;
    private final String branch;
    private final String instanceName;
    private final String api;
    private final int cors;
    private final long startTime;
    private boolean apiWorks;
    private boolean frontEndWorks;

    public Instance(String frontEnd, String version, String commit, String branch, String instanceName, String api, int cors, long startTime) {
        this.frontEnd = frontEnd;
        this.version = version;
        this.commit = commit;
        this.branch = branch;
        this.instanceName = instanceName;
        this.api = api;
        this.cors = cors;
        this.startTime = startTime;
    }

    public JSONObject toJSON() {
        JSONObject instanceJSON = new JSONObject();
        instanceJSON.put("version", this.version);
        instanceJSON.put("commit", this.commit);
        instanceJSON.put("branch", this.branch);
        instanceJSON.put("name", this.instanceName);
        instanceJSON.put("api", this.api);
        instanceJSON.put("cors", this.cors);
        instanceJSON.put("startTime", Long.valueOf(this.startTime));
        instanceJSON.put("api_online", this.apiWorks);
        instanceJSON.put("frontend_online", this.frontEndWorks);
        instanceJSON.put("frontEnd", Objects.requireNonNullElse(frontEnd, "None"));
        return instanceJSON;
    }

    public boolean testApi() {
        JSONObject body = new JSONObject();
        body.put("url", "https://www.youtube.com/watch?v=kpwNjdEPz7E");
        String postResponse = RequestUtil.sendPost(body, "https://" + this.api + "/api/json");
        if (postResponse == null) {
            return false;
        }
        JSONObject responseJSON = new JSONObject(postResponse);
        return responseJSON.has("url");
    }

    public boolean testFrontEnd() {
        return RequestUtil.testUrl("https://" + frontEnd);
    }

    public String toString() {
        return this.frontEnd;
    }

    public void apiWorks(boolean works) {
        this.apiWorks = works;
    }

    public void frontEndWorks(boolean works) {
        this.frontEndWorks = works;
    }

    public String branch() {
        return this.branch;
    }

    public int cors() {
        return this.cors;
    }


    public String commit() {
        return this.commit;
    }

    public String name() {
        return this.instanceName;
    }

    public String version() {
        return this.version;
    }

    public boolean doesApiWork() {
        return this.apiWorks;
    }

    public boolean doesFrontEndWork() {
        return this.frontEndWorks;
    }

    public String api() {
        return this.api;
    }

    public String frontEnd() {
        return this.frontEnd;
    }

    public String markdown() {
        String template = "[url](https://url)";
        return template.replaceAll("url", frontEnd);
    }
}
