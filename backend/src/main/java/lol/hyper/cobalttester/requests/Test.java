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
    private final String authorization;
    private final Logger logger = LogManager.getLogger(Test.class, CobaltTester.MESSAGE_FACTORY);
    private int attempts = 0;

    public Test(Instance instance, String service, String testUrl, String authorization) {
        this.instance = instance;
        this.service = service;
        this.testUrl = testUrl;
        this.authorization = authorization;
    }

    public void run() {
        if (service.equalsIgnoreCase("Frontend")) {
            runFrontEndTest();
        } else {
            runApiTest();
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
        if (instance.getVersion().startsWith("10.")) {
            api = protocol + "://" + instance.getApi();
        } else {
            api = protocol + "://" + instance.getApi() + "/api/json";
        }
        JSONObject postContents = new JSONObject();
        postContents.put("url", testUrl);
        RequestResults testResponse = RequestUtil.sendPost(postContents, api, authorization);
        String content = testResponse.responseContent();
        int responseCode = testResponse.responseCode();
        Exception exception = testResponse.exception();
        // check if there are exceptions first
        if (exception != null) {
            logger.warn("Test FAIL for {} with {} - HTTP {}, reason={}", api, service, responseCode, exception.toString());
            instance.addResult(new TestResult(service, false, exception.toString()));
            return;
        }
        // check if the content returned was null
        if (content == null) {
            logger.warn("Test FAIL for {} with {} - HTTP {}, response content returned null", api, service, responseCode);
            instance.addResult(new TestResult(service, false, "Response content returned null from API"));
            return;
        }
        // make sure the json response is valid
        JSONObject jsonResponse;
        try {
            jsonResponse = new JSONObject(content);
        } catch (JSONException jsonException) {
            logger.warn("Test FAIL for {} with {} - HTTP {}, reason={}", api, service, responseCode, jsonException.toString());
            instance.addResult(new TestResult(service, false, jsonException.toString()));
            return;
        }
        // get the status of the API request
        // just in case it randomly doesn't return
        String status;
        if (jsonResponse.has("status")) {
            status = jsonResponse.getString("status");
        } else {
            status = "UNKNOWN";
        }
        // count the attempts here, since the previous ones failed
        attempts++;
        // if the API's response was HTTP 200, it most likely worked
        if (responseCode == 200) {
            // if the response has a status key, then read it. otherwise, it probably failed
            if (status == null) {
                logger.warn("Test FAIL for {} with {} - HTTP 200, status=INVALID", api, service);
                instance.addResult(new TestResult(service, false, "Status returned null, HTTP " + responseCode));
                return;
            }

            // if the API's status was redirect/stream/tunnel/success/picker, it was successful at the request
            if (status.equalsIgnoreCase("redirect") || status.equalsIgnoreCase("stream") || status.equalsIgnoreCase("success") || status.equalsIgnoreCase("picker") || status.equalsIgnoreCase("tunnel")) {
                logger.info("Test PASS for {} with {} - HTTP 200, status={}", api, service, status);
                instance.addResult(new TestResult(service, true, "Working, returned valid status and HTTP 200"));
            } else {
                logger.info("Test FAIL for {} with {} - HTTP 200, status={}", api, service, status);
                instance.addResult(new TestResult(service, false, "Status returned " + status));
            }
        } else {
            // if we didn't get back a 200 response, it failed
            String errorMessage;
            // there SHOULD be an error message, so parse it
            // cobalt 7 vs. 10 sends it back differently
            if (jsonResponse.has("error")) {
                JSONObject errorBody = jsonResponse.getJSONObject("error");
                errorMessage = errorBody.getString("code");
            } else if (jsonResponse.has("text")) {
                errorMessage = jsonResponse.getString("text");
            } else {
                errorMessage = "Unknown error, could not parse error from API";
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
                } catch (InterruptedException interruptedException) {
                    logger.error("Rate-limit retry interrupted for {} with {}", api, service, interruptedException);
                }
                return;
            }
            logger.warn("Test FAIL for {} with {} - HTTP {}, status=error, reason={}", api, service, responseCode, errorMessage);
            instance.addResult(new TestResult(service, false, errorMessage));
        }
    }

    @Override
    public String toString() {
        return instance.getApi() + ":" + service;
    }
}
