package lol.hyper.cobalttester.utils;

import lol.hyper.cobalttester.instance.Instance;
import lol.hyper.cobalttester.requests.TestResult;
import lol.hyper.cobalttester.services.Services;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.*;
import java.util.regex.Pattern;

public class StringUtil {


    /**
     * Make an instance table.
     *
     * @param instances The instances to use.
     * @param type      domain, ip, or official.
     * @return The HTML table.
     */
    public static String buildMainTables(List<Instance> instances, String type) {
        StringBuilder table = new StringBuilder();
        // build the table for output
        table.append("<div class=\"table-container\"><table id=\"sort-table\">\n<tr><th onclick=\"sortTable(0, event)\">Frontend</th><th onclick=\"sortTable(1, event)\">API</th><th onclick=\"sortTable(2, event)\">Version</th><th onclick=\"sortTable(3, event)\">Commit</th><th onclick=\"sortTable(4, event)\">Branch</th><th onclick=\"sortTable(5, event)\">Name</th><th onclick=\"sortTable(6, event)\">CORS</th><th onclick=\"sortTable(7, event)\">Score</th></tr>\n");

        List<Instance> filtered = FilterUtils.filter(instances, type);

        // build each element for the table
        for (Instance instance : filtered) {
            // get basic information
            String version = instance.getVersion();
            String name = instance.getName();
            int cors = instance.getCors();
            String score = Double.toString(instance.getScore()).split("\\.")[0] + "%";
            String commit;
            String api;
            String branch;
            String frontEnd;
            if (instance.isApiWorking()) {
                commit = "<a href=\"https://github.com/imputnet/cobalt/commit/" + instance.getCommit() + "\">" + instance.getCommit() + "</a>";
                api = "<a href=\"{{ site.url }}" + "/instance/" + instance.getHash() + "\">" + instance.getApi() + "</a>";
                branch = "<a href=\"https://github.com/imputnet/cobalt/tree/" + instance.getBranch() + "\">" + instance.getBranch() + "</a>";
                if (instance.getFrontEnd() == null) {
                    frontEnd = "<a href=\"{{ site.url }}" + "/instance/" + instance.getHash() + "\">None</a>";
                } else {
                    frontEnd = "<a href=\"{{ site.url }}" + "/instance/" + instance.getHash() + "\">" + instance.getFrontEnd() + "</a>";
                }
            } else {
                commit = instance.getCommit();
                api = instance.getApi();
                branch = instance.getBranch();
                if (instance.getFrontEnd() == null) {
                    frontEnd = "None";
                } else {
                    frontEnd = instance.getFrontEnd();
                }
            }

            // add the instance elements
            switch (instance.getTrustStatus()) {
                case "safe": {
                    table.append("<tr class=\"safe\">");
                    break;
                }
                case "unknown": {
                    table.append("<tr class=\"unknown\">");
                    break;
                }
                case "not_safe": {
                    table.append("<tr class=\"not-safe\">");
                    break;
                }
                case "offline": {
                    table.append("<tr>");
                    break;
                }
            }
            table.append("<td>").append(frontEnd).append("</td>");
            table.append("<td>").append(api).append("</td>");
            table.append("<td>").append(version).append("</td>");
            table.append("<td>").append(commit).append("</td>");
            table.append("<td>").append(branch).append("</td>");
            table.append("<td>").append(name).append("</td>");
            table.append("<td>").append(cors).append("</td>");
            // if the score is at least 0, that means we ran tests, link these tests
            if (instance.getScore() >= 0) {
                String scoreLink = "<a href=\"{{ site.url }}/instance/" + instance.getHash() + "\">" + score + "</a>";
                table.append("<td>").append(scoreLink).append("</td>");
            } else {
                // score was -1, which means we did not run any tests, so do not link the instance page
                table.append("<td>").append("Offline").append("</td>");
            }
            table.append("</tr>");
        }
        table.append("</table></div>");
        return table.toString();
    }

    public static String buildScoreTable(Instance instance) {
        StringBuilder table = new StringBuilder();
        // build the table for output
        table.append("<div class=\"table-container\"><table class=\"service-table\">\n<tr><th>Service</th><th>Working?</th><th>Status</th></tr>\n");

        // make it sort correctly
        instance.getTestResults().sort(Comparator.comparing(TestResult::service));

        for (TestResult result : instance.getTestResults()) {
            String service = result.service();
            boolean working = result.status();
            switch (service) {
                case "Reddit", "Instagram", "YouTube", "YouTube Music", "YouTube Shorts" -> service = service + "*";
            }
            String serviceLink = "<a href=\"{{ site.url }}/service/" + Services.makeSlug(service).replace("*", "") + "\">" + service + "</a>";
            table.append("<tr><td>").append(serviceLink).append("</td>");
            if (working) {
                table.append("<td>").append("✅").append("</td>").append("<td>Working</td>");
            } else {
                table.append("<td>").append("❌").append("</td>").append("<td>").append(makeLogPretty(result.message())).append("</td>");
            }
            table.append("</tr>");
        }
        table.append("</table></div>");
        return table.toString();
    }

    public static String buildServiceTable(List<Instance> instances, String service, String type) {
        List<Instance> filtered = FilterUtils.filter(instances, type);
        // Make them in alphabetical order
        Collections.sort(filtered);
        // Store which instance works with this service
        Map<Instance, Boolean> workingInstances = new HashMap<>();
        for (Instance instance : filtered) {
            boolean working = instance.getTestResults().stream().filter(testResult -> testResult.service().equals(service)).map(TestResult::status).findFirst().orElse(false);
            workingInstances.put(instance, working);
        }

        StringBuilder table = new StringBuilder();
        // build the table for output
        table.append("<div class=\"table-container\"><table id=\"sort-table\">\n<tr><th onclick=\"sortTable(0, event)\">Frontend</th><th onclick=\"sortTable(1, event)\">API</th><th onclick=\"sortTable(2, event)\">Working?</th></tr>\n");

        for (Map.Entry<Instance, Boolean> pair : workingInstances.entrySet()) {
            Instance instance = pair.getKey();
            boolean working = pair.getValue();
            String frontEnd;
            String api;
            if (instance.isApiWorking()) {
                api = "<a href=\"{{ site.url }}" + "/instance/" + instance.getHash() + "\">" + instance.getApi() + "</a>";
                if (instance.getFrontEnd() == null) {
                    frontEnd = "<a href=\"{{ site.url }}" + "/instance/" + instance.getHash() + "\">None</a>";
                } else {
                    frontEnd = "<a href=\"{{ site.url }}" + "/instance/" + instance.getHash() + "\">" + instance.getFrontEnd() + "</a>";
                }
            } else {
                api = instance.getApi();
                if (instance.getFrontEnd() == null) {
                    frontEnd = "None";
                } else {
                    frontEnd = instance.getFrontEnd();
                }
            }
            switch (instance.getTrustStatus()) {
                case "safe": {
                    table.append("<tr class=\"safe\">");
                    break;
                }
                case "unknown": {
                    table.append("<tr class=\"unknown\">");
                    break;
                }
                case "not_safe": {
                    table.append("<tr class=\"not-safe\">");
                    break;
                }
                case "offline": {
                    table.append("<tr>");
                    break;
                }
            }
            table.append("<td>").append(frontEnd).append("</td>");
            table.append("<td>").append(api).append("</td>");
            if (working) {
                table.append("<td>").append("✅").append("</td>");
            } else {
                table.append("<td>").append("❌").append("</td>");
            }
            table.append("</tr>");
        }
        table.append("</table></div>");
        return table.toString();
    }

    /**
     * Generates the partial hash of a string.
     *
     * @param input The input.
     * @return Partial section of the hash. Used as an ID system.
     */
    public static String makeHash(String input) {
        String hash = DigestUtils.sha256Hex(input).toLowerCase(Locale.ROOT);
        return hash.substring(0, 10);
    }

    /**
     * Remove HTML tags in a string.
     *
     * @param input The input.
     * @return The string back with no HTML tags.
     */
    public static String removeHtml(String input) {
        return input.replaceAll("<[^>]+>", "");
    }

    public static String makeLogPretty(String input) {
        if (input == null) {
            return "";
        }

        Map<Pattern, String> errorMessages = new HashMap<>();
        errorMessages.put(Pattern.compile("(?i)youtube"), "Failed due to missing YouTube cookies");
        errorMessages.put(Pattern.compile("(?i)not supported"), "Instance does not support this service");
        errorMessages.put(Pattern.compile("(?i)find anything about this|something went wrong|i don't see anything|fetch.fail|fetch.critical"), "Failed to load media, this can be for a lot of reasons");
        errorMessages.put(Pattern.compile("(?i)soundcloud"), "Failed to fetch temporary token for download");
        errorMessages.put(Pattern.compile("(?i)service api"), "Failed to connect to service API");
        errorMessages.put(Pattern.compile("(?i)requests|rate_exceeded"), "Rate limited by instance");
        errorMessages.put(Pattern.compile("(?i)jwt.missing"), "Missing JWT, use instance frontend to access");
        errorMessages.put(Pattern.compile("(?i)fetch.empty"), "This service did not return anything to download");
        errorMessages.put(Pattern.compile("(?i)SocketTimeoutException|timed_out"), "Timed out or this request was too slow");
        errorMessages.put(Pattern.compile("(?i)JSONException"), "API returned invalid JSON");
        errorMessages.put(Pattern.compile("(?i)tweet"), "Unable to find media in tweet");

        for (Map.Entry<Pattern, String> entry : errorMessages.entrySet()) {
            if (entry.getKey().matcher(input).find()) {
                return entry.getValue();
            }
        }
        return removeHtml(input);
    }
}
