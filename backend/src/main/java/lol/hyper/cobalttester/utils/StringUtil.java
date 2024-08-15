package lol.hyper.cobalttester.utils;

import lol.hyper.cobalttester.instance.Instance;
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
            // check if score is empty
            if (instance.getTestResults().isEmpty()) {
                table.append("<td>").append(score).append("</td>");
            } else {
                String scoreLink = "<a href=\"{{ site.url }}/instance/" + instance.getHash() + "\">" + score + "</a>";
                table.append("<td>").append(scoreLink).append("</td>");
            }
            table.append("</tr>");
        }
        table.append("</table></div>");
        return table.toString();
    }

    public static String buildScoreTable(Instance instance) {
        StringBuilder table = new StringBuilder();
        // build the table for output
        table.append("<div class=\"table-container\"><table>\n<tr><th>Service</th><th>Working?</th></tr>\n");

        for (Map.Entry<String, Boolean> pair : instance.getTestResults().entrySet()) {
            String service = pair.getKey();
            switch (service) {
                case "Reddit", "Instagram", "YouTube", "YouTube Music", "YouTube Shorts" -> service = service + "*";
            }
            boolean result = pair.getValue();
            String serviceLink = "<a href=\"{{ site.url }}/service/" + Services.makeSlug(service).replace("*", "") + "\">" + service + "</a>";
            table.append("<tr><td>").append(serviceLink).append("</td>");
            if (result) {
                table.append("<td>").append("✅").append("</td>");
            } else {
                table.append("<td>").append("❌").append("</td>");
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
            Boolean working = instance.getTestResults().get(service);
            // if the API is offline, this will return null.
            // force set this service to be false.
            if (working == null) {
                working = false;
            }
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
}
