package lol.hyper.cobalttester;

import lol.hyper.cobalttester.tools.FileUtil;
import lol.hyper.cobalttester.tools.RequestUtil;
import lol.hyper.cobalttester.tools.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class CobaltTester {

    public static Logger logger;
    public static ArrayList<Instance> instances = new ArrayList<>();
    public static final String USER_AGENT = "CobaltTester (+https://instances.hyper.lol)";

    public static void main(String[] args) {
        System.setProperty("log4j.configurationFile", "log4j2config.xml");
        logger = LogManager.getLogger(CobaltTester.class);
        File instancesFile = new File("instances");
        if (!instancesFile.exists()) {
            logger.error("Unable to find 'instances' file!");
            System.exit(1);
        }
        JSONArray cacheArray = new JSONArray();
        File cacheFile = new File("instances.json");
        // delete the file if it exists
        if (cacheFile.exists()) {
            boolean deleteStatus = cacheFile.delete();
            if (deleteStatus) {
                logger.info("Deleted instances.json");
            } else {
                logger.error("Unable to delete instances.json");
            }
        }

        ArrayList<String> instanceFileContents = FileUtil.readInstances(instancesFile);
        if (instanceFileContents == null) {
            logger.error("Unable to read instance file! Exiting...");
            System.exit(1);
        }

        for (String line : instanceFileContents) {
            List<String> lineFix = Arrays.asList(line.split(","));
            String api = lineFix.get(0);
            String frontEnd = lineFix.get(1);
            String protocol = lineFix.get(2);

            if (frontEnd.equals("None")) {
                frontEnd = null;
            }
            buildInstance(frontEnd, api, protocol);
        }

        // Sort the list in descending order based on the version
        instances.sort((instance1, instance2) -> {
            String[] version1Array = instance1.version().replace("-dev", "").split("\\.");
            String[] version2Array = instance2.version().replace("-dev", "").split("\\.");

            for (int i = 0; i < Math.min(version1Array.length, version2Array.length); i++) {
                int result = Integer.compare(Integer.parseInt(version2Array[i]), Integer.parseInt(version1Array[i]));
                if (result != 0) {
                    return result;
                }
            }

            return Integer.compare(version2Array.length, version1Array.length);
        });

        // write to json file
        for (Instance instance : instances) {
            cacheArray.put(instance.toJSON());
        }
        FileUtil.writeFile(cacheArray, cacheFile);

        // edit the template with the tables
        String template = FileUtil.readFile(new File("template.md"));
        if (template == null) {
            logger.error("Unable to read template.md! Exiting...");
            System.exit(1);
        }
        String domainTable = StringUtil.makeTable(new ArrayList<>(instances), "domain");
        String ipTable = StringUtil.makeTable(new ArrayList<>(instances), "ip");
        template = template.replace("<TABLE>", domainTable);
        template = template.replace("<TABLE2>", ipTable);
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        template = template.replace("<TIME>", f.format(new Date()));
        FileUtil.writeFile(template, new File("index.md"));
    }

    public static void buildInstance(String frontEnd, String baseApi, String protocol) {
        String version;
        String commit;
        String branch;
        String name;
        int cors = 0;
        long startTime;
        String fullApiURL = protocol + "://" + baseApi + "/api/serverInfo";
        JSONObject apiRequest = RequestUtil.requestJSON(fullApiURL);
        if (apiRequest == null) {
            version = "-1";
            commit = null;
            branch = null;
            name = null;
            startTime = -1;
        } else {
            try {
                version = apiRequest.getString("version");
                commit = apiRequest.getString("commit");
                branch = apiRequest.getString("branch");
                name = apiRequest.getString("name");
                if (apiRequest.has("cors")) {
                    cors = apiRequest.getInt("cors");
                }
                startTime = apiRequest.getLong("startTime");
            } catch (JSONException exception) {
                logger.error("Unable to process " + baseApi, exception);
                return;
            }
        }
        // build the instance
        Instance instance = new Instance(frontEnd, baseApi, protocol, version, commit, branch, name, cors, startTime);
        logger.info("Frontend: " + frontEnd);
        logger.info("API: " + baseApi);
        if (instance.testApi()) {
            instance.apiWorks(true);
            logger.info("API status: true");
        } else {
            instance.apiWorks(false);
            logger.info("API status: false");
        }

        if (instance.testFrontEnd()) {
            instance.frontEndWorks(true);
            logger.info("Frontend status: true");
        } else {
            instance.frontEndWorks(false);
            logger.info("Frontend status: false");
        }
        logger.info("-----------------------------------------");
        instances.add(instance);
    }
}
