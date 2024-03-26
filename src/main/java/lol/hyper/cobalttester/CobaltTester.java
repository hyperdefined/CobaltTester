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

    public static void main(String[] args) throws IOException {
        System.setProperty("log4j.configurationFile", "log4j2config.xml");
        logger = LogManager.getLogger(CobaltTester.class);
        File instancesFile = new File("instances");
        if (!instancesFile.exists()) {
            logger.error("Unable to find 'instances' file!");
            System.exit(1);
        }
        ArrayList<Instance> instances = new ArrayList<>();
        JSONArray cache = new JSONArray();
        File cacheFile = new File("instances.json");
        // delete the file if it exists
        if (cacheFile.exists()) {
            boolean deleteStatus = cacheFile.delete();
            if (deleteStatus) {
                logger.info("Deleted cache.json");
            } else {
                logger.error("Unable to delete cache.json");
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
                String fullURL = "https://" + api + "/api/serverInfo";

                if (frontEnd.equals("None")) {
                    frontEnd = null;
                }

                JSONObject request = RequestUtil.requestJSON(fullURL);
                if (request != null) {
                    String version = request.getString("version");
                    String commit = request.getString("commit");
                    String branch = request.getString("branch");
                    String name = request.getString("name");
                    int cors = request.getInt("cors");
                    long startTime = request.getLong("startTime");
                    Instance instance = new Instance(frontEnd, version, commit, branch, name, api, cors, startTime);
                    logger.info("Frontend: " + frontEnd);
                    logger.info("API: " + api);
                    if (instance.test()) {
                        instance.works(true);
                        logger.info("Working: true");
                    } else {
                        logger.info("Working: false");
                        instance.works(false);
                    }
                    logger.info("-----------------------------------------");
                    instances.add(instance);
                } else {
                    logger.error("Unable to get JSON from " + fullURL);
                    Instance instance = new Instance(frontEnd, "-1", null, null, null, api, -1, -1L);
                    instances.add(instance);
                }
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
        table.append("| Frontend | API | Version | Commit | Branch | Name | CORS | Status |\n");
        table.append("| --- | --- | --- | --- | --- | --- | --- | --- |\n");
        for (Instance instance : instances) {
            String status = instance.doesWork() ? "Online" : "Offline";
            // does not have a front end
            if (instance.frontEnd() == null) {
                cache.put(instance.toJSON());
                table.append("| ").append("None");
            } else {
                cache.put(instance.toJSON());
                table.append("| ").append(instance.markdown());
            }
            table.append(" | ").append(instance.api()).append(" | ").append(instance.version()).append(" | ").append(instance.commit()).append(" | ").append(instance.branch()).append(" | ").append(instance.name()).append(" | ").append(instance.cors()).append(" | ").append(status).append(" |\n");
        }
        JSONUtil.writeFile(cache, cacheFile);
        JSONUtil.replaceSelected(table.toString());


        try {
            executeCommand("rm", "-r", "_site");
            executeCommand("bundle", "exec", "jekyll", "build");
        } catch (InterruptedException exception) {
            logger.error("Unable to build site!", exception);
        }
    }

    private static void executeCommand(String... command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
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
}
