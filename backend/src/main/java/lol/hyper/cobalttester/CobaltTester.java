package lol.hyper.cobalttester;

import lol.hyper.cobalttester.tools.FileUtil;
import lol.hyper.cobalttester.tools.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CobaltTester {

    public static Logger logger;
    public static final String USER_AGENT = "CobaltTester (+https://instances.hyper.lol)";

    public static void main(String[] args) {
        System.setProperty("log4j.configurationFile", "log4j2config.xml");
        logger = LogManager.getLogger(CobaltTester.class);

        int availableThreads = Runtime.getRuntime().availableProcessors();
        logger.info("Total available threads: " + availableThreads);

        File instancesFile = new File("instances");
        File blockedInstances = new File("blocked_instances");
        File testUrlsFile = new File("test_urls");
        if (!instancesFile.exists()) {
            logger.error("Unable to find 'instances' file!");
            System.exit(1);
        }
        JSONArray cacheArray = new JSONArray();
        File cacheFile = new File("../web", "instances.json");
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
        ArrayList<String> blockedInstancesContents = FileUtil.readInstances(blockedInstances);
        ArrayList<String> testUrlsContents = FileUtil.readInstances(testUrlsFile);
        if (instanceFileContents == null) {
            logger.error("Unable to read instance file! Exiting...");
            System.exit(1);
        }
        if (blockedInstancesContents == null) {
            logger.error("Unable to read blocked instance file! Exiting...");
            System.exit(1);
        }
        if (testUrlsContents == null) {
            logger.error("Unable to read test urls file! Exiting...");
            System.exit(1);
        }

        ArrayList<Instance> instances = new ArrayList<>();
        for (String line : instanceFileContents) {
            List<String> lineFix = Arrays.asList(line.split(","));
            String api = lineFix.get(0);
            String frontEnd = lineFix.get(1);
            String protocol = lineFix.get(2);

            boolean apiBlocked = blockedInstancesContents.stream().anyMatch(api::contains);
            boolean frontEndBlocked = blockedInstancesContents.stream().anyMatch(frontEnd::contains);
            if (apiBlocked || frontEndBlocked) {
                logger.warn("Skipping instance " + api + " because it's blocked.");
                continue;
            }

            if (frontEnd.equals("None")) {
                frontEnd = null;
            }
            Instance newInstance = new Instance(frontEnd, api, protocol);
            instances.add(newInstance);
            logger.info("Creating instance " + api);
        }

        int totalTasks = instances.size();
        logger.info("Total tasks to process: " + totalTasks);

        int maxThreads = 10;
        int threads = Math.min(maxThreads, totalTasks);
        int tasksPerThread = totalTasks / threads;
        int remainderTasks = totalTasks % threads;
        logger.info("Using " + threads + " threads");
        logger.info("Putting " + tasksPerThread + " tasks on each thread");
        logger.info("Left over: " + remainderTasks);

        CountDownLatch latch = new CountDownLatch(threads);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        int startTask = 0;
        for (int i = 0; i < threads; i++) {
            int extraTask = (i < remainderTasks) ? 1 : 0; // Distribute remainder tasks
            int endTask = startTask + tasksPerThread + extraTask;
            executor.submit(new Tester(startTask, endTask, latch, instances, i, testUrlsContents));
            startTask = endTask;
        }

        try {
            latch.await();
            logger.info("All threads have completed!!!!");
            executor.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        instances.sort(Comparator.comparingDouble(Instance::getScoreResults).reversed());

        // write to json file
        for (Instance instance : instances) {
            cacheArray.put(instance.toJSON());
        }
        FileUtil.writeFile(cacheArray, cacheFile);

        // edit the template with the tables
        String template = FileUtil.readFile(new File("../web", "template.md"));
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
        FileUtil.writeFile(template, new File("../web", "index.md"));
    }
}
