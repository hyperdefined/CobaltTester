package lol.hyper.cobalttester.requests;

import lol.hyper.cobalttester.instance.Instance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class Test {

    private final Instance instance;
    private final String service;
    private final String testUrl;
    private final Logger logger = LogManager.getLogger(this);

    public Test(Instance instance, String service, String testUrl) {
        this.instance = instance;
        this.service = service;
        this.testUrl = testUrl;
    }

    public Instance getInstance() {
        return instance;
    }

    public String getService() {
        return service;
    }

    public String getTestUrl() {
        return testUrl;
    }

    public void run() {
        String protocol = instance.getProtocol();
        String api = protocol + "://" + instance.getApi() + "/api/json";
        // if the api is down, don't run this test
        if (!instance.isApiWorking()) {
            return;
        }
        JSONObject postContents = new JSONObject();
        postContents.put("url", testUrl);
        RequestResults testResponse = RequestUtil.sendPost(postContents, api);
        // if the URL did not return HTTP 200, it did not pass
        if (testResponse.responseCode() != 200) {
            logger.warn("Test FAIL for {} with code {} with {}", api, testResponse.responseCode(), testUrl);
            instance.addResult(service, false);
        } else {
            // test passed
            logger.info("Test PASS for {} with {}", api, testUrl);
            instance.addResult(service, true);
        }
    }
}
