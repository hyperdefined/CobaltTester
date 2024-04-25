package lol.hyper.cobalttester;

import lol.hyper.cobalttester.tools.RequestUtil;
import org.json.JSONObject;

import java.util.Objects;

public class Instance {

    private final String frontEnd;
    private final String api;
    private final String protocol;
    private final String version;
    private final String commit;
    private final String branch;
    private final String name;
    private final int cors;
    private final long startTime;
    private boolean apiWorks;
    private boolean frontEndWorks;

    public Instance(String frontEnd, String api, String protocol, String version, String commit, String branch, String name, int cors, long startTime) {
        this.frontEnd = frontEnd;
        this.api = api;
        this.protocol = protocol;
        this.version = version;
        this.commit = commit;
        this.branch = branch;
        this.name = name;
        this.cors = cors;
        this.startTime = startTime;
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
        instanceJSON.put("api_online", this.apiWorks);
        instanceJSON.put("frontend_online", this.frontEndWorks);
        instanceJSON.put("frontEnd", Objects.requireNonNullElse(frontEnd, "None"));
        instanceJSON.put("protocol", protocol);
        return instanceJSON;
    }

    public boolean testApi() {
        JSONObject body = new JSONObject();
        body.put("url", "https://twitter.com/TweetsOfCats/status/1783249644975169656");
        String postResponse = RequestUtil.sendPost(body, protocol + "://" + this.api + "/api/json");
        if (postResponse == null) {
            return false;
        }
        JSONObject responseJSON = new JSONObject(postResponse);
        if (responseJSON.getString("status").equalsIgnoreCase("redirect")) {
            return responseJSON.getString("url").contains("https://video.twimg.com");
        }
        return false;
    }

    public boolean testFrontEnd() {
        if (frontEnd == null) {
            return false;
        }
        return RequestUtil.testUrl(protocol + "://" + frontEnd);
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
        return this.name;
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

    public String protocol() {
        return this.protocol;
    }
}
