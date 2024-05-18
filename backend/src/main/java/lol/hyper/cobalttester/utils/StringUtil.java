package lol.hyper.cobalttester.utils;

import lol.hyper.cobalttester.instance.Instance;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class StringUtil {


    /**
     * Make an instance table.
     *
     * @param instances The instances to use.
     * @param type      "domain" OR "ip". If you want IPs only, use "ip". If you want domains only, use "domain"
     * @return The HTML table.
     */
    public static String buildMainTables(List<Instance> instances, String type) {
        StringBuilder table = new StringBuilder();
        // build the table for output
        table.append("<div class=\"table-container\"><table>\n<tr><th>Frontend</th><th>API</th><th>Version</th><th>Commit</th><th>Branch</th><th>Name</th><th>CORS</th><th>Score</th><th>Status</th></tr>\n");

        Iterator<Instance> copyIterator = instances.iterator();
        // if type is IP, remove all domains and keep IPs only
        if (type.equalsIgnoreCase("ip")) {
            while (copyIterator.hasNext()) {
                Instance instance = copyIterator.next();
                // remove if it's a domain
                if (!isIP(instance.getApi())) {
                    copyIterator.remove();
                }
            }
        }
        // if type is domain, remove all IPs and keep domains only
        if (type.equalsIgnoreCase("domain")) {
            while (copyIterator.hasNext()) {
                Instance instance = copyIterator.next();
                // remove if it's an IP
                if (isIP(instance.getApi())) {
                    copyIterator.remove();
                }
            }
        }

        // build each element for the table
        for (Instance instance : instances) {
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
            String commit = instance.getCommit();
            String branch = instance.getBranch();
            String name = instance.getName();
            int cors = instance.getCors();
            String status = "Unknown";
            String score = Double.toString(instance.getScore()).split("\\.")[0] + "%";
            // if both api and frontend online, report it online
            if (instance.isApiWorking() && instance.isFrontEndWorking()) {
                status = "Online";
                table.append("\n<tr class=\"status-online\">");
            }
            // if either api or frontend are offline, report it partial status
            if (!instance.isApiWorking() || !instance.isFrontEndWorking()) {
                status = "Partial";
                // if both api and frontend are offline, report it offline status
                if (!instance.isApiWorking() && !instance.isFrontEndWorking()) {
                    status = "Offline";
                    table.append("\n<tr class=\"status-offline\">\n");
                } else {
                    table.append("\n<tr class=\"status-partial\">");
                }
            }
            // add the instance elements
            table.append("<td>").append(frontEnd).append("</td>");
            table.append("<td>").append(api).append("</td>");
            table.append("<td>").append(version).append("</td>");
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
            table.append("<td>").append(status).append("</td></tr>");
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
            boolean result = pair.getValue();
            table.append("<tr><td>").append(service).append("</td>");
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

    /**
     * Check if a given string is an IP or not.
     *
     * @param domain The string to test.
     * @return true/false if it's an IP.
     */
    public static boolean isIP(String domain) {
        String ipv4Pattern = "(?<![\\d.])(?:(?:[1-9]?\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(?:[1-9]?\\d|1\\d\\d|2[0-4]\\d|25[0-5])(?![\\d.])";
        String ipv4WithPortPattern = ipv4Pattern + "(:\\d{1,5})?";
        return Pattern.matches(ipv4WithPortPattern, domain);
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
