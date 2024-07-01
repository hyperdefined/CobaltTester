package lol.hyper.cobalttester.requests;

import lol.hyper.cobalttester.instance.Instance;
import lol.hyper.cobalttester.services.Services;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class Tester implements Runnable {

    private final int startTask;
    private final int endTask;
    private final CountDownLatch latch;
    private final List<Instance> instances;
    private final int threadNumber;
    private final Services services;
    private final Logger logger = LogManager.getLogger(this);

    public Tester(int startTask, int endTask, CountDownLatch latch, List<Instance> instances, int threadNumber, Services services) {
        this.startTask = startTask;
        this.endTask = endTask;
        this.latch = latch;
        this.instances = instances;
        this.threadNumber = threadNumber;
        this.services = services;
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
                JSONObject apiInfo = RequestUtil.requestJSON(api);
                // load the api information
                if (apiInfo != null) {
                    instance.setApiWorking(true);
                    getApiInfo(apiInfo, instance);
                }
                try {
                    performTests(instance);
                } catch (InterruptedException exception) {
                    logger.error("Unable to sleep thread", exception);
                }
                logger.info("Finished tests for " + instance.getApi());
            }
        } finally {
            latch.countDown();
            logger.info("Tasks finished for thread " + threadNumber + ". There are " + latch.getCount() + " threads left to process.");
        }
    }

    private void getApiInfo(JSONObject apiJson, Instance instance) {
        if (apiJson.has("name")) {
            String name = apiJson.getString("name");
            instance.setName(StringEscapeUtils.escapeHtml4(name));
        }
        if (apiJson.has("version")) {
            String version = apiJson.getString("version");
            // older instances had -dev in the version
            if (version.contains("-dev")) {
                version = version.replace("-dev", "");
            }
            instance.setVersion(StringEscapeUtils.escapeHtml4(version));
        }
        if (apiJson.has("commit")) {
            String commit = apiJson.getString("commit");
            instance.setCommit(StringEscapeUtils.escapeHtml4(commit));
        }
        if (apiJson.has("branch")) {
            String branch = apiJson.getString("branch");
            instance.setBranch(StringEscapeUtils.escapeHtml4(branch));
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
        int totalTests = services.getTests().size();
        boolean checkFrontEnd = instance.getFrontEnd() != null;
        String api = instance.getProtocol() + "://" + instance.getApi() + "/api/json";
        // if the api is working, perform the tests
        if (instance.isApiWorking()) {
            // perform a POST request for each url
            for (Map.Entry<String, String> testPair : services.getTests().entrySet()) {
                String service = testPair.getKey();
                String serviceUrl = testPair.getValue();
                JSONObject postContents = new JSONObject();
                postContents.put("url", serviceUrl);
                RequestResults testResponse = RequestUtil.sendPost(postContents, api);
                // if the URL did not return HTTP 200, it did not pass
                if (testResponse.responseCode() != 200) {
                    logger.warn("Test FAIL for " + api + " with code " + testResponse.responseCode() + " with " + serviceUrl);
                    instance.addResult(service, false);
                    continue;
                }
                // since it returned HTTP 200, it passed
                logger.info("Test PASS for " + api + " with " + serviceUrl);
                score++;
                instance.addResult(service, true);
                Thread.sleep(2000);
            }
        }
        // if the frontend exists, add it to the tests
        if (checkFrontEnd) {
            totalTests++;
            String frontEnd = instance.getProtocol() + "://" + instance.getFrontEnd();
            boolean testFrontEnd = RequestUtil.testUrl(frontEnd);
            if (testFrontEnd) {
                score++;
                instance.setFrontEndWorking(true);
                logger.info("Test PASS for checking " + frontEnd);
                instance.addResult("Frontend", true);
            } else {
                logger.warn("Test FAILED for checking " + frontEnd);
            }
        }
        double finalScore = 0;
        if (totalTests == 0) {
            instance.setScore(finalScore);
        } else {
            finalScore = (double) score / totalTests * 100.0;
        }
        logger.info("Final score is " + finalScore);
        instance.setScore(finalScore);
    }
}
