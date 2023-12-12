package lol.hyper.cobalttester;

import org.json.JSONObject;

public class Instance {

    private final String frontEnd;
    private final String version;
    private final String commit;
    private final String branch;
    private final String instanceName;
    private final String url;
    private final int cors;
    private final long startTime;
    private boolean works;

    public Instance(String frontEnd, String version, String commit, String branch, String instanceName, String url, int cors, long startTime) {
        this.frontEnd = frontEnd;
        this.version = version;
        this.commit = commit;
        this.branch = branch;
        this.instanceName = instanceName;
        this.url = url;
        this.cors = cors;
        this.startTime = startTime;
        this.works = false;
    }

    public JSONObject toJSON() {
        JSONObject instanceJSON = new JSONObject();
        instanceJSON.put("version", this.version);
        instanceJSON.put("commit", this.commit);
        instanceJSON.put("branch", this.branch);
        instanceJSON.put("name", this.instanceName);
        instanceJSON.put("url", this.url);
        instanceJSON.put("cors", this.cors);
        instanceJSON.put("startTime", this.startTime);
        instanceJSON.put("status", this.works);
        instanceJSON.put("frontEnd", this.frontEnd);
        return instanceJSON;
    }

    public boolean test() {
        JSONObject body = new JSONObject();
        body.put("url", "https://www.youtube.com/watch?v=kpwNjdEPz7E");
        String postResponse = RequestUtil.sendPost(body, "https://" + this.url + "/api/json");
        if (postResponse == null) {
            return false;
        }
        JSONObject responseJSON = new JSONObject(postResponse);
        return responseJSON.has("url");
    }

    public String toString() {
        return this.url;
    }

    public void works(boolean works) {
        this.works = works;
    }

    public String branch() {
        return this.branch;
    }

    public String url() {
        return this.url;
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

    public boolean doesWork() {
        return this.works;
    }

    public String frontEnd() {
        if (frontEnd.equals("None")) {
            return "None";
        }
        String template = "[url](https://url)";
        return template.replaceAll("url", this.frontEnd);
    }
}
