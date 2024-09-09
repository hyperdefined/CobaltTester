package lol.hyper.cobalttester.requests;

import lol.hyper.cobalttester.instance.Instance;
import lol.hyper.cobalttester.utils.StringUtil;
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
        if (validFrontEnd) {
            logger.info("Test PASS for checking frontend {} ", testUrl);
        } else {
            logger.info("Test FAIL for checking frontend {} ", testUrl);
        }
        instance.addResult(new TestResult(service, validFrontEnd, null));
    }

    private void runApiTest() {
        String protocol = instance.getProtocol();
        String api;
        if (instance.isNew()) {
            api = protocol + "://" + instance.getApi();
        } else {
            api = protocol + "://" + instance.getApi() + "/api/json";
        }
        JSONObject postContents = new JSONObject();
        postContents.put("url", testUrl);
        RequestResults testResponse = RequestUtil.sendPost(postContents, api);
        if (testResponse.responseContent() == null) {
            logger.warn("Test FAIL for {} with {} - HTTP {}, request returned null", api, service, testResponse.responseCode());
            return;
        }
        JSONObject jsonResponse = new JSONObject(testResponse.responseContent());
        String status = jsonResponse.getString("status");
        // count the attempts
        attempts++;
        // if the API's response was HTTP 200, it most likely worked
        if (testResponse.responseCode() == 200) {
            // if we couldn't get the status from the response, it failed
            if (status == null) {
                logger.warn("Test FAIL for {} with {} - HTTP 200, status=INVALID", api, service);
                instance.addResult(new TestResult(service, false, "Status returned null, HTTP " + testResponse.responseCode()));
                return;
            }

            // if the API's status was redirect/stream/success/picker, it was successful
            if (status.equalsIgnoreCase("redirect") || status.equalsIgnoreCase("stream") || status.equalsIgnoreCase("success") || status.equalsIgnoreCase("picker") || status.equalsIgnoreCase("tunnel")) {
                logger.info("Test PASS for {} with {} - HTTP 200, status={}", api, service, status);
                instance.addResult(new TestResult(service, true, "Working, returned valid status and HTTP 200"));
            } else {
                logger.info("Test FAIL for {} with {} - HTTP 200, status={}", api, service, status);
                instance.addResult(new TestResult(service, false, "Status returned " + status));
            }
        } else {
            // if we didn't get back a 200 response, it failed
            if (status == null) {
                logger.warn("Test FAIL for {} with {} - HTTP {}, status=INVALID", api, service, testResponse.responseCode());
                instance.addResult(new TestResult(service, false, "Status returned null"));
                return;
            }

            // output what the error was
            String errorMessage;
            if (jsonResponse.has("error")) {
                JSONObject errorBody = jsonResponse.getJSONObject("error");
                errorMessage = errorBody.getString("code");
            } else {
                errorMessage = jsonResponse.getString("text");
            }
            // if we got rate limited, rerun the test in a few seconds
            if (status.equalsIgnoreCase("rate-limit") || errorMessage.contains("rate_exceeded")) {
                if (attempts >= 5) {
                    logger.warn("Test FAIL for {} with {} - attempts limit REACHED with {} tries", api, service, attempts);
                    instance.addResult(new TestResult(service, false, "Rate limited, max attempts reached"));
                    return;
                }
                long secondsToWait = 3 + (attempts);
                logger.warn("Test RATE-LIMITED for {} with {} - trying again in {} seconds, attempts={}", api, service, secondsToWait, attempts);
                try {
                    Thread.sleep(secondsToWait * 1000);
                    runApiTest();
                } catch (InterruptedException exception) {
                    logger.error("Rate-limit retry interrupted for {} with {}", api, service, exception);
                }
                return;
            }
            logger.warn("Test FAIL for {} with {} - HTTP {}, status=error, reason={}", api, service, testResponse.responseCode(), errorMessage);
            instance.addResult(new TestResult(service, false, "API returned error status, HTTP " + testResponse.responseCode()));
        }
    }
}
