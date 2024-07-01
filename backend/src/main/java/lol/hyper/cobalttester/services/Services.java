package lol.hyper.cobalttester.services;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Services {

    private final JSONObject tests;
    private final Map<String, String> testsUrls = new HashMap<>();

    public Services(JSONObject tests) {
        this.tests = tests;
    }

    public void importTests() {
        for (String key : tests.keySet()) {
            testsUrls.put(key, tests.getString(key));
        }
    }

    public static String makeSlug(String service) {
        String slug = service.toLowerCase(Locale.ROOT);
        return slug.replace(" ", "-");
    }

    public Map<String, String> getTests() {
        return testsUrls;
    }
}
