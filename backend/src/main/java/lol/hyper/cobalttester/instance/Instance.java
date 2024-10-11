package lol.hyper.cobalttester.instance;

import lol.hyper.cobalttester.requests.TestResult;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
    private boolean frontEndWorking;
    private double score;
    private String hash;
    private boolean is10;
    private boolean turnstile;

    private final List<TestResult> testResults = new ArrayList<>();

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
        instanceJSON.put("frontend_online", this.frontEndWorking);
        instanceJSON.put("frontEnd", Objects.requireNonNullElse(frontEnd, "None"));
        instanceJSON.put("protocol", protocol);
        instanceJSON.put("score", score);
        instanceJSON.put("trust", trustStatus);
        instanceJSON.put("turnstile", turnstile);
        JSONObject workingServices = new JSONObject();
        for (TestResult result : testResults) {
            String service = result.service().toLowerCase(Locale.ROOT).replace(" ", "_");
            // skip frontend here
            if (service.equalsIgnoreCase("Frontend")) {
                continue;
            }
            workingServices.put(service, result.status());
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

    public boolean is10() {
        return is10;
    }

    public void setIs10(boolean is10) {
        this.is10 = is10;
    }

    public void setTurnstile(boolean turnstile) {
        this.turnstile = turnstile;
    }

    public boolean hasTurnstile() {
        return turnstile;
    }

    public void setFrontEndWorking(boolean status) {
        this.frontEndWorking = status;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    public List<TestResult> getTestResults() {
        return testResults;
    }

    public void addResult(TestResult testResult) {
        testResults.add(testResult);
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
        long workingServices = testResults.stream().filter(TestResult::status).count();
        int totalTestsRan = testResults.size();
        if (totalTestsRan > 0) {
            score = ((double) workingServices / totalTestsRan) * 100;
        } else {
            score = 0;
        }
    }

    public void setOffline() {
        this.setApiWorking(false);
        this.setTrustStatus("offline");
        this.setName("Offline");
        this.setCommit("Offline");
        this.setBranch("Offline");
        this.setName("Offline");
        this.setVersion("Offline");
    }

    @Override
    public int compareTo(Instance instance) {
        return this.api.compareTo(instance.api);
    }
}
