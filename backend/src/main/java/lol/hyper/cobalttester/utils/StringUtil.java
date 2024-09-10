package lol.hyper.cobalttester.utils;

import lol.hyper.cobalttester.instance.Instance;
import lol.hyper.cobalttester.requests.TestResult;
import lol.hyper.cobalttester.services.Services;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.*;

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
        table.append("<div class=\"table-container\"><table>\n<tr><th>Frontend</th><th>API</th><th>Version</th><th>Commit</th><th>Branch</th><th>Name</th><th>CORS</th><th>Score</th></tr>\n");

        List<Instance> filtered = FilterUtils.filter(instances, type);

        // build each element for the table
        for (Instance instance : filtered) {
            // does not have a front end
            String frontEnd;
            if (instance.getFrontEnd() == null) {
                frontEnd = "None";
            } else {
                frontEnd = "<a href=\"" + instance.getProtocol() + "://" + instance.getFrontEnd() + "\">" + instance.getFrontEnd() + "</a>";
            }
            // get basic information
            String api = "<a href=\"" + instance.getProtocol() + "://" + instance.getApi() + "/api/serverInfo\">" + instance.getApi() + "</a>";
            String version = instance.getVersion();
            String branch = instance.getBranch();
            String name = instance.getName();
            int cors = instance.getCors();
            String score = Double.toString(instance.getScore()).split("\\.")[0] + "%";
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
            String commit;
            if (instance.isApiWorking()) {
                commit = "<a href=\"https://github.com/imputnet/cobalt/commit/" + instance.getCommit() + "\">" + instance.getCommit() + "</a>";
            } else {
                commit = instance.getCommit();
            }
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
        table.append("<div class=\"table-container\"><table>\n<tr><th>Service</th><th>Working?</th><th>Status</th></tr>\n");

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
        table.append("<div class=\"table-container\"><table>\n<tr><th>Frontend</th><th>API</th><th>Working?</th></tr>\n");

        for (Map.Entry<Instance, Boolean> pair : workingInstances.entrySet()) {
            Instance instance = pair.getKey();
            boolean working = pair.getValue();
            String frontEnd;
            if (instance.getFrontEnd() == null) {
                frontEnd = "None";
            } else {
                frontEnd = "<a href=\"" + instance.getProtocol() + "://" + instance.getFrontEnd() + "\">" + instance.getFrontEnd() + "</a>";
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
            String api = "<a href=\"" + instance.getProtocol() + "://" + instance.getApi() + "/api/serverInfo\">" + instance.getApi() + "</a>";
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
        if (input.contains("youtube")) {
            return "Failed due to missing YouTube cookies";
        }
        if (input.contains("not supported")) {
            return "Instance does not support this service";
        }
        if (input.contains("find anything about this") || input.contains("something went wrong when")) {
            return "Failed to fetch media";
        }
        if (input.contains("soundcloud")) {
            return "Failed to fetch temporary token for download";
        }
        if (input.contains("service api")) {
            return "Failed to connect to service API";
        }
        if (input.contains("requests") || input.contains("rate_exceeded")) {
            return "Rate limited by instance";
        }
        if (input.contains("jwt.missing")) {
            return "Missing JWT";
        }
        return removeHtml(input);
    }
}
