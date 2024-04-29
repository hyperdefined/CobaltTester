package lol.hyper.cobalttester;

import lol.hyper.cobalttester.tools.RequestUtil;
import lol.hyper.cobalttester.tools.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Tester implements Runnable {

    private final int startTask;
    private final int endTask;
    private final CountDownLatch latch;
    private final List<Instance> instances;
    private final int threadNumber;
    private final List<String> testUrls;
    private final Logger logger = LogManager.getLogger(this);

    public Tester(int startTask, int endTask, CountDownLatch latch, List<Instance> instances, int threadNumber, List<String> testUrls) {
        this.startTask = startTask;
        this.endTask = endTask;
        this.latch = latch;
        this.instances = instances;
        this.threadNumber = threadNumber;
        this.testUrls = testUrls;
    }

    @Override
    public void run() {
        logger.info("Starting thread for instances: " + instances.subList(startTask, endTask));
        // process the range ot tests to do
        try {
            for (int i = startTask; i < endTask; i++) {
                Instance instance = instances.get(i);
                String protocol = instance.getProtocol();
                String api = protocol + "://" + instance.getApi() + "/api/serverInfo";
                // make sure the API works before testing
                boolean testApi = RequestUtil.testUrl(api);
                instance.setApiWorking(testApi);
                // if the api is offline, don't perform tests on this instance
                if (!testApi) {
                    logger.warn("Skipping " + api + " tests because it's offline.");
                    continue;
                }
                // load the JSON from the api
                JSONObject apiInfo = RequestUtil.requestJSON(api);
                if (apiInfo == null) {
                    logger.warn("Skipping " + api + " tests because the API JSON returned null");
                    continue;
                }
                // load the api information
                getApiInfo(apiInfo, instance);
                try {
                    performTests(instance);
                } catch (InterruptedException exception) {
                    logger.error("Unable to sleep thread", exception);
                }
            }
        } finally {
            latch.countDown();
            logger.info("Tasks finished for thread " + threadNumber + ". There are " + latch.getCount() + " threads left to process.");
        }
    }

    private void getApiInfo(JSONObject apiJson, Instance instance) {
        if (apiJson.has("name")) {
            String name = apiJson.getString("name");
            if (StringUtil.check(name)) {
                instance.setName(name);
            } else {
                instance.setName("invalid-name");
                logger.warn(instance.getApi() + " has an invalid name!");
            }
        }
        if (apiJson.has("version")) {
            String version = apiJson.getString("version");
            // older instances had -dev in the version
            if (version.contains("-dev")) {
                version = version.replace("-dev", "");
            }
            if (version.matches("^[0-9.]+$")) {
                // use the version from the JSON itself
                // just in case we replaced it
                instance.setVersion(apiJson.getString("version"));
            } else {
                instance.setVersion("invalid-version");
                logger.warn(instance.getApi() + " has an invalid version!");
            }
        }
        if (apiJson.has("commit")) {
            String commit = apiJson.getString("commit");
            if (StringUtil.check(commit)) {
                instance.setCommit(commit);
            } else {
                instance.setCommit("invalid-commit");
                logger.warn(instance.getApi() + " has an invalid commit!");
            }
        }
        if (apiJson.has("branch")) {
            String branch = apiJson.getString("branch");
            if (StringUtil.check(branch)) {
                instance.setBranch(branch);
            } else {
                instance.setBranch("invalid-branch");
                logger.warn(instance.getApi() + " has an invalid branch!");
            }
        }
        if (apiJson.has("cors")) {
            int cors = apiJson.getInt("cors");
            if (cors == 0 || cors == 1) {
                instance.setCors(cors);
            } else {
                instance.setCors(-1);
                logger.warn(instance.getApi() + " has an invalid cors!");
            }
        }
        if (apiJson.has("startTime")) {
            String startTimeString = String.valueOf(apiJson.getLong("startTime"));
            if (startTimeString.matches("[0-9]+")) {
                instance.setStartTime(apiJson.getLong("startTime"));
            } else {
                instance.setStartTime(0L);
                logger.warn(instance.getApi() + " has an invalid startTime!");
            }
        }
    }

    private void performTests(Instance instance) throws InterruptedException {
        int score = 0;
        int totalTests = testUrls.size();
        boolean checkFrontEnd = instance.getFrontEnd() != null;
        String api = instance.getProtocol() + "://" + instance.getApi() + "/api/json";
        // perform a POST request for each url
        for (String url : testUrls) {
            JSONObject postContents = new JSONObject();
            postContents.put("url", url);
            RequestResults testResponse = RequestUtil.sendPost(postContents, api);
            // if the URL did not return HTTP 200, it did not pass
            if (testResponse.responseCode() != 200) {
                logger.warn("Test FAIL for " + api + " with code " + testResponse.responseCode() + " with " + url);
                continue;
            }
            // since it returned HTTP 200, it passed
            logger.info("Test PASS for " + api + " with " + url);
            score++;
            Thread.sleep(5000);
        }
        // if the frontend exists, check it here
        // add that to the tests as well
        if (checkFrontEnd) {
            totalTests++;
            String frontEnd = instance.getProtocol() + "://" + instance.getFrontEnd();
            boolean testFrontEnd = RequestUtil.testUrl(frontEnd);
            if (testFrontEnd) {
                score++;
                instance.setFrontEndWorking(true);
                logger.info("Test PASS for checking " + frontEnd);
            } else {
                logger.warn("Test FAILED for checking " + frontEnd);
            }
        }
        double finalScore = (double) score / totalTests * 100.0;
        instance.setScore(finalScore);
    }
}
