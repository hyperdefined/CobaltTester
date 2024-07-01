package lol.hyper.cobalttester.utils;

import lol.hyper.cobalttester.instance.Instance;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class FilterUtils {

    public static List<Instance> filter(List<Instance> instances, String filter) {
        Iterator<Instance> copyIterator = instances.iterator();
        // if type is IP, remove all domains and keep IPs only
        if (filter.equalsIgnoreCase("ip")) {
            while (copyIterator.hasNext()) {
                Instance instance = copyIterator.next();
                // remove if it's a domain
                if (!isIP(instance.getApi())) {
                    copyIterator.remove();
                }
            }
        }
        // if type is domain, remove all IPs and keep domains only
        if (filter.equalsIgnoreCase("domain")) {
            while (copyIterator.hasNext()) {
                Instance instance = copyIterator.next();
                // remove if it's an IP
                if (isIP(instance.getApi())) {
                    copyIterator.remove();
                }
                if (instance.getApi().equals("api.cobalt.tools")) {
                    copyIterator.remove();
                }
            }
        }
        // sort out the main one
        if (filter.equalsIgnoreCase("official")) {
            while (copyIterator.hasNext()) {
                Instance instance = copyIterator.next();
                if (!instance.getApi().equals("api.cobalt.tools")) {
                    // remove everything but this one
                    copyIterator.remove();
                }
            }
        }
        return instances;
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
}
