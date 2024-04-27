package lol.hyper.cobalttester;

import lol.hyper.cobalttester.tools.RequestUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class Tester implements Runnable {

    private final int startTask;
    private final int endTask;
    private final CountDownLatch latch;
    private final ArrayList<Instance> instances;
    private final int threadNumber;
    private final Logger logger = LogManager.getLogger(this);
    private final String[] testUrls = {
        "https://www.youtube.com/watch?v=b3rFbkFjRrA",
        "https://music.youtube.com/watch?v=iYJoahPxhR8",
        "https://www.tiktok.com/@hancorecantaim/video/7298613260780195079", 
        "https://www.instagram.com/linustech/reel/C6CJa5rvM47/",
        "https://x.com/PepitoTheCat/status/1783716906618294596",
        "https://www.reddit.com/r/TikTokCringe/comments/wup1fg/id_be_escaping_at_the_first_chance_i_got/",
        "https://soundcloud.com/rick-astley-official/never-gonna-give-you-up-4",
        "https://www.bilibili.com/video/BV1Ti421m7sM?spm_id_from=333.1007.tianma.1-2-2.click",
        "https://www.dailymotion.com/video/x8xjm74",
        "https://ok.ru/video/7533205195294",
        "https://streamable.com/p7blpp",
        "https://www.tumblr.com/viralfrog/748037133842939904"
    };

    public Tester(int startTask, int endTask, CountDownLatch latch, ArrayList<Instance> instances, int threadNumber) {
        this.startTask = startTask;
        this.endTask = endTask;
        this.latch = latch;
        this.instances = instances;
        this.threadNumber = threadNumber;
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
                boolean testApi = RequestUtil.testUrl(api);
                instance.setApiWorking(testApi);
                // if the api is offline, don't perform tests on this instance
                if (!testApi) {
                    logger.warn("Skipping " + api + " tests because it's offline.");
                    continue;
                }
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
            instance.setName(apiJson.getString("name"));
        }
        if (apiJson.has("version")) {
            instance.setVersion(apiJson.getString("version"));
        }
        if (apiJson.has("commit")) {
            instance.setCommit(apiJson.getString("commit"));
        }
        if (apiJson.has("branch")) {
            instance.setBranch(apiJson.getString("branch"));
        }
        if (apiJson.has("cors")) {
            instance.setCors(apiJson.getInt("cors"));
        }
        if (apiJson.has("startTime")) {
            instance.setStartTime(apiJson.getLong("startTime"));
        }
    }

    private void performTests(Instance instance) throws InterruptedException {
        int score = 0;
        int totalTests = testUrls.length;
        String api = instance.getProtocol() + "://" + instance.getApi() + "/api/json";
        // if the frontend exists, add that to the tests
        if (instance.getFrontEnd() != null) {
            totalTests++;
        }
        for (String url : testUrls) {
            JSONObject postContents = new JSONObject();
            postContents.put("url", url);
            JSONObject testResponse = RequestUtil.sendPost(postContents, api);
            if (testResponse == null || !testResponse.has("status")) {
                logger.warn("Test FAILED for " + api + " with " + url);
                continue;
            }
            String status = testResponse.getString("status");
            switch (status) {
                // any of these means the api worked
                case "redirect", "stream", "success" -> {
                    logger.info("Test PASS for " + api + " with " + url);
                    score++;
                }
                case "rate-limit" -> logger.info("Test RATE-LIMITED for " + api);
            }
            Thread.sleep(5000);
        }
        if (instance.getFrontEnd() != null) {
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
        instance.setScore(score);
        instance.setTestsRan(totalTests);
    }
}
