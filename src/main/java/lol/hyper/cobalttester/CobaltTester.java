package lol.hyper.cobalttester;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CobaltTester {

    public static Logger logger;
    public static ArrayList<Instance> instances = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        System.setProperty("log4j.configurationFile", "log4j2config.xml");
        logger = LogManager.getLogger(CobaltTester.class);
        File instancesFile = new File("instances");
        if (!instancesFile.exists()) {
            logger.error("Unable to find 'instances' file!");
            System.exit(1);
        }
        JSONArray cache = new JSONArray();
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
        try (LineIterator it = FileUtils.lineIterator(instancesFile, "UTF-8");) {
            while (it.hasNext()) {
                String line = it.nextLine();
                if (line.startsWith("#")) {
                    continue;
                }

                List<String> lineFix = Arrays.asList(line.split(","));
                String api = lineFix.get(0);
                String frontEnd = lineFix.get(1);

                if (frontEnd.equals("None")) {
                    frontEnd = null;
                }
                buildInstance(frontEnd, api);

            }
        } catch (IOException exception) {
            logger.error("Unable to read contents of instances file!", exception);
        }

        // Sort the list in descending order based on the version
        instances.sort((instance1, instance2) -> {
            String[] version1Array = instance1.version().split("\\.");
            String[] version2Array = instance2.version().split("\\.");

            for (int i = 0; i < Math.min(version1Array.length, version2Array.length); i++) {
                int result = Integer.compare(Integer.parseInt(version2Array[i]), Integer.parseInt(version1Array[i]));
                if (result != 0) {
                    return result;
                }
            }

            return Integer.compare(version2Array.length, version1Array.length);
        });


        StringBuilder table = new StringBuilder();
        // build the table for output
        table.append("<table>\n<tr><th>Frontend</th><th>API</th><th>Version</th><th>Commit</th><th>Branch</th><th>Name</th><th>CORS</th><th>Status</th></tr>\n");
        for (Instance instance : instances) {
            // does not have a front end
            String frontEnd;
            if (instance.frontEnd() == null) {
                cache.put(instance.toJSON());
                frontEnd = "None";
            } else {
                cache.put(instance.toJSON());
                frontEnd = "<a href=\"https://" + instance.frontEnd() + "\">" + instance.frontEnd() + "</a>";
            }
            String api = "<a href=\"https://" + instance.api() + "/api/serverInfo\">" + instance.frontEnd() + "</a>";
            String version = instance.version();
            String commit = instance.commit();
            String branch = instance.branch();
            String name = instance.name();
            int cors = instance.cors();
            String status = "Unknown";
            // if both api and frontend online, report it online
            if (instance.doesApiWork() && instance.doesFrontEndWork()) {
                status = "Online";
                table.append("\n<tr class=\"status-online\">");
            }
            // if either api or frontend are offline, report it partial status
            if (!instance.doesApiWork() || !instance.doesFrontEndWork()) {
                status = "Partial";
                // if both api and frontend are offline, report it offline status
                if (!instance.doesApiWork() && !instance.doesFrontEndWork()) {
                    status = "Offline";
                    table.append("\n<tr class=\"status-offline\">\n");
                } else {
                    table.append("\n<tr class=\"status-partial\">");
                }
            }
            table.append("<td>").append(frontEnd).append("</td>");
            table.append("<td>").append(api).append("</td>");
            table.append("<td>").append(version).append("</td>");
            table.append("<td>").append(commit).append("</td>");
            table.append("<td>").append(branch).append("</td>");
            table.append("<td>").append(name).append("</td>");
            table.append("<td>").append(cors).append("</td>");
            table.append("<td>").append(status).append("</td></tr>");
        }
        table.append("</table>");
        JSONUtil.writeFile(cache, cacheFile);
        JSONUtil.replaceSelected(table.toString());


        try {
            executeCommand("rm", "-r", "_site");
            executeCommand("bundle", "exec", "jekyll", "build");
            executeCommand("cp", "instances.json", "_site");
        } catch (InterruptedException exception) {
            logger.error("Unable to build site!", exception);
        }
    }

    private static void executeCommand(String... command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        logger.info("Running " + processBuilder.command());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info(line);
            }
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.error(line);
            }
        }

        int exitCode = process.waitFor();

        logger.info("Exit Code: " + exitCode);
    }

    public static void buildInstance(String frontEnd, String baseApi) {
        String fullApiURL = "https://" + baseApi + "/api/serverInfo";
        JSONObject request = RequestUtil.requestJSON(fullApiURL);
        if (request != null) {
            String version = request.getString("version");
            String commit = request.getString("commit");
            String branch = request.getString("branch");
            String name = request.getString("name");
            int cors = request.getInt("cors");
            long startTime = request.getLong("startTime");
            Instance instance = new Instance(frontEnd, version, commit, branch, name, baseApi, cors, startTime);
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
        } else {
            logger.error("Unable to get JSON from " + fullApiURL);
            Instance instance = new Instance(frontEnd, "-1", null, null, null, baseApi, -1, -1L);
            instances.add(instance);
        }
    }
}
