package lol.hyper.cobalttester.requests;

import lol.hyper.cobalttester.CobaltTester;
import lol.hyper.cobalttester.instance.Instance;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class ApiCheck {

    private final Instance instance;
    private final Logger logger = LogManager.getLogger(ApiCheck.class, CobaltTester.MESSAGE_FACTORY);

    public ApiCheck(Instance instance) {
        this.instance = instance;
    }

    public void run() {
        String protocol = instance.getProtocol();
        String api = instance.getApi();

        logger.info("Checking API status for {}", api);
        String responseContent;
        JSONObject json;
        String url = protocol + "://" + api;
        // check the base of the domain first (for cobalt 10)
        RequestResults requestResults = RequestUtil.getStatusCode(url);
        int responseCode = requestResults.responseCode();
        if (responseCode == 200) {
            // Load the API information
            responseContent = RequestUtil.requestJSON(url).responseContent();
            // if it fails to load any content (this should never happen with 200)
            if (responseContent == null) {
                logger.warn("Root content null for {}", api);
                instance.setOffline();
                return;
            }
            // if it fails to parse, try /api/serverInfo
            try {
                json = new JSONObject(responseContent);
            } catch (JSONException exception) {
                logger.warn("Failed to parse root for {}, trying /api/serverInfo", api);
                responseContent = RequestUtil.requestJSON(url + "/api/serverInfo").responseContent();
                if (responseContent == null) {
                    logger.warn("Response null for {} on /api/serverInfo", api);
                    instance.setOffline();
                    return;
                }
                try {
                    json = new JSONObject(responseContent);
                } catch (JSONException exception2) {
                    // we tried everything, mark it dead
                    logger.warn("Failed to parse /api/serverInfo for {}", api);
                    instance.setOffline();
                    return;
                }
            }
        } else {
            // check the older serverInfo response (base returned not 200)
            url = url + "/api/serverInfo";
            responseContent = RequestUtil.requestJSON(url).responseContent();
            // if it fails to load any content
            if (responseContent == null) {
                logger.warn("Response null for {} on /api/serverInfo", api);
                instance.setOffline();
                return;
            }
            // make sure we can parse it
            try {
                json = new JSONObject(responseContent);
            } catch (JSONException exception2) {
                logger.warn("Failed to parse serverInfo for {}", api);
                // we tried everything, mark it dead
                instance.setOffline();
                return;
            }
        }

        instance.setApiWorking(true);
        // on cobalt 10, the JSON response is different
        if (json.has("cobalt")) {
            loadNewApi(json);
            return;
        }

        if (json.has("version")) {
            String version = json.getString("version");
            // older instances had -dev in the version
            if (version.contains("-dev")) {
                version = version.replace("-dev", "");
            }
            instance.setVersion(StringEscapeUtils.escapeHtml4(version));
        } else {
            logger.warn("{} is online, but failed to get version", api);
            instance.setOffline();
            return;
        }

        if (json.has("name")) {
            String name = json.getString("name");
            instance.setName(StringEscapeUtils.escapeHtml4(name));
        }
        if (json.has("commit")) {
            String commit = json.getString("commit");
            instance.setCommit(StringEscapeUtils.escapeHtml4(commit));
        }
        if (json.has("branch")) {
            String branch = json.getString("branch");
            instance.setBranch(StringEscapeUtils.escapeHtml4(branch));
        }
        if (json.has("cors")) {
            int cors = json.getInt("cors");
            if (cors == 0 || cors == 1) {
                instance.setCors(cors);
            } else {
                instance.setCors(-1);
                logger.warn("{} has an invalid cors!", api);
            }
        }
        if (json.has("startTime")) {
            String startTimeString = String.valueOf(json.getLong("startTime"));
            if (startTimeString.matches("[0-9]+")) {
                instance.setStartTime(json.getLong("startTime"));
            } else {
                instance.setStartTime(0L);
                logger.warn("{} has an invalid startTime!", api);
            }
        }
    }

    private void loadNewApi(JSONObject response) {
        instance.setIs10(true);
        JSONObject cobalt = response.getJSONObject("cobalt");
        instance.setName("N/A");
        if (cobalt.has("version")) {
            instance.setVersion(StringEscapeUtils.escapeHtml4(cobalt.getString("version")));
        } else {
            instance.setVersion("Unknown");
        }
        instance.setCors(1);
        JSONObject git = response.getJSONObject("git");
        instance.setBranch(StringEscapeUtils.escapeHtml4(git.getString("branch")));
        instance.setCommit(StringEscapeUtils.escapeHtml4(git.getString("commit").substring(0, 6)));
        String remote = git.getString("remote");
        if (!remote.equalsIgnoreCase("imputnet/cobalt")) {
            logger.warn("{} is running a FORK, remote is {}", instance.getApi(), remote);
        }

        // on cobalt 10, check to see if the instance has turnstile on
        // if it's enabled, then mark all tests as fail
        RequestResults sessionResult = RequestUtil.sendPost(new JSONObject(), instance.getProtocol() + "://" + instance.getApi() + "/session", null);
        JSONObject sessionJSON;
        try {
            sessionJSON = new JSONObject(sessionResult.responseContent());
        } catch (JSONException exception) {
            // this really should NEVER happen but you never know
            logger.error("Unable to check session for {}", instance.getApi(), exception);
            instance.setApiWorking(false);
            return;
        }
        JSONObject error = sessionJSON.getJSONObject("error");
        if (error.getString("code").contains("turnstile.missing")) {
            instance.setTurnstile(true);
            logger.warn("{} has turnstile enabled!", instance.getApi());
        }
    }

    @Override
    public String toString() {
        return instance.getApi() + ":" + "check";
    }
}
