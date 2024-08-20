package lol.hyper.cobalttester.requests;

import lol.hyper.cobalttester.CobaltTester;
import lol.hyper.cobalttester.instance.Instance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class Test {

    private final Instance instance;
    private final String service;
    private final String testUrl;
    private final Logger logger = LogManager.getLogger(this);
    private int attempts = 0;

    public Test(Instance instance, String service, String testUrl) {
        this.instance = instance;
        this.service = service;
        this.testUrl = testUrl;
    }

    public void run() {
        if (service.equalsIgnoreCase("Frontend")) {
            runFrontEndTest();
        } else {
            runApiTest();
        }
    }

    private String getStatus(String response) {
        JSONObject json;
        try {
            json = new JSONObject(response);
        } catch (JSONException exception) {
            return null;
        }
        if (json.has("status")) {
            return json.getString("status");
        } else {
            return null;
        }
    }

    private void runFrontEndTest() {
        boolean validFrontEnd = RequestUtil.testFrontEnd(testUrl);
        instance.addResult(service, validFrontEnd);
    }

    private void runApiTest() {
        String protocol = instance.getProtocol();
        String api = protocol + "://" + instance.getApi() + "/api/json";
        // if the api is down, don't run this test
        if (!instance.isApiWorking()) {
            return;
        }
        JSONObject postContents = new JSONObject();
        postContents.put("url", testUrl);
        RequestResults testResponse = RequestUtil.sendPost(postContents, api);
        if (testResponse.responseContent() == null) {
            logger.warn("Test FAIL for {} with {} - HTTP {}, request returned null", api, testUrl, testResponse.responseCode());
            return;
        }
        String status = getStatus(testResponse.responseContent());
        // count the attempts
        attempts++;
        // if the API's response was HTTP 200, it most likely worked
        if (testResponse.responseCode() == 200) {
            // if we couldn't get the status from the response, it failed
            if (status == null) {
                logger.warn("Test FAIL for {} with {} - HTTP 200, status=INVALID", api, testUrl);
                instance.addResult(service, false);
                return;
            }

            // if the API's status was redirect/stream/success/picker, it was successful
            if (status.equalsIgnoreCase("redirect") || status.equalsIgnoreCase("stream") || status.equalsIgnoreCase("success") || status.equalsIgnoreCase("picker")) {
                logger.info("Test PASS for {} with {} - HTTP 200, status={}", api, testUrl, status);
                instance.addResult(service, true);
            }
        } else {
            // if we didn't get back a 200 response, it failed
            if (status == null) {
                logger.warn("Test FAIL for {} with {} - HTTP {}, status=INVALID", api, testUrl, testResponse.responseCode());
                instance.addResult(service, false);
                return;
            }
            // if we got rate limited, rerun the test in a few seconds
            if (status.equalsIgnoreCase("rate-limit")) {
                if (attempts >= 5) {
                    logger.warn("Test FAIL for {} with {} - attempts limit REACHED with {} tries", api, testUrl, attempts);
                    return;
                }
                long secondsToWait = 3 + (attempts);
                logger.warn("Test RATE-LIMITED for {} with {} - trying again in {} seconds, attempts={}", api, testUrl, secondsToWait, attempts);
                try {
                    Thread.sleep(secondsToWait * 1000);
                    runApiTest();
                } catch (InterruptedException exception) {
                    logger.error("Rate-limit retry interrupted for {} with {}", api, testUrl, exception);
                }
                return;
            }
            logger.warn("Test FAIL for {} with {} - HTTP {}, status={}", api, testUrl, testResponse.responseCode(), status);
            instance.addResult(service, false);
        }
    }
}
