package lol.hyper.cobalttester;

import lol.hyper.cobalttester.instance.Instance;
import lol.hyper.cobalttester.instance.Tester;
import lol.hyper.cobalttester.utils.FileUtil;
import lol.hyper.cobalttester.utils.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CobaltTester {

    public static Logger logger;
    public static String USER_AGENT = "CobaltTester-<commit> (+https://instances.hyper.lol)";
    public static JSONObject config;

    public static void main(String[] args) {
        System.setProperty("log4j.configurationFile", "log4j2config.xml");
        logger = LogManager.getLogger(CobaltTester.class);

        String commit = null;
        try {
            commit = getCommit();
        } catch (IOException | GitAPIException exception) {
            logger.error("Unable to get git repo information", exception);
            System.exit(1);
        }

        if (commit == null) {
            logger.error("Unable to get git repo information, returned null.");
            System.exit(1);
        }

        logger.info("CobaltTester starting up.");
        logger.info("CobaltTester running commit: " + commit);
        USER_AGENT = USER_AGENT.replace("<commit>", commit);

        boolean buildWeb = false;
        if (args.length != 0) {
            if (args[0].equalsIgnoreCase("web")) {
                buildWeb = true;
            }
        }

        logger.info("Running with args: " + Arrays.toString(args));

        // Output how many threads we can use
        int availableThreads = Runtime.getRuntime().availableProcessors();
        logger.info("Total available threads: " + availableThreads);

        // load the config
        config = getConfig();
        if (config == null) {
            logger.error("Unable to load config! Exiting...");
            System.exit(1);
        }

        // load some files
        File instancesFile = new File("instances");
        File blockedInstances = new File("blocked_instances");
        File testUrlsFile = new File("test_urls");
        List<String> instanceFileContents = FileUtil.readRawFile(instancesFile);
        List<String> blockedInstancesContents = FileUtil.readRawFile(blockedInstances);
        List<String> testUrlsContents = FileUtil.readRawFile(testUrlsFile);
        // make sure all files exist
        if (instanceFileContents.isEmpty()) {
            logger.error("Instance file returned empty. Does it exist?");
            System.exit(1);
        }
        if (testUrlsContents.isEmpty()) {
            logger.error("Test URLs file returned empty. Does it exist?");
            System.exit(1);
        }

        // shuffle the lists here
        Collections.shuffle(instanceFileContents);
        Collections.shuffle(testUrlsContents);

        // load the instance file and build each instance
        List<Instance> instances = new ArrayList<>();
        for (String line : instanceFileContents) {
            // each line is formatted api,frontend,protocol
            // we can split this and get each part
            List<String> lineFix = Arrays.asList(line.split(","));
            String api = lineFix.get(0);
            String frontEnd = lineFix.get(1);
            String protocol = lineFix.get(2);

            // make sure the instance is not in the blocked file
            if (!blockedInstancesContents.isEmpty()) {
                boolean apiBlocked = blockedInstancesContents.stream().anyMatch(api::contains);
                boolean frontEndBlocked = blockedInstancesContents.stream().anyMatch(frontEnd::contains);
                // if it is, remove it
                if (apiBlocked || frontEndBlocked) {
                    logger.warn("Skipping instance " + api + " because it's blocked.");
                    continue;
                }
            }

            // if the instance has "None" set for the frontend
            if (frontEnd.equals("None")) {
                frontEnd = null;
            }
            // build the instance
            Instance newInstance = new Instance(frontEnd, api, protocol);
            newInstance.setHash(StringUtil.makeHash(api));
            instances.add(newInstance);
            logger.info("Creating instance " + api);
        }

        int totalTasks = instances.size();
        logger.info("Total tasks to process: " + totalTasks);

        // calculate how many tasks per thread
        // a task is a group of instances to test
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
        // distribute each group of tasks on a thread and run them
        for (int i = 0; i < threads; i++) {
            int extraTask = (i < remainderTasks) ? 1 : 0;
            int endTask = startTask + tasksPerThread + extraTask;
            executor.submit(new Tester(startTask, endTask, latch, instances, i, testUrlsContents));
            startTask = endTask;
        }

        try {
            // wait for all threads to end
            latch.await();
            logger.info("All threads have completed!!!!");
            executor.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        instances.sort(Comparator.comparingDouble(Instance::getScore).reversed());

        SimpleDateFormat f = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedDate = f.format(new Date());

        // remove the old instance.json file
        JSONArray cacheArray = new JSONArray();
        File cacheFile = new File(config.getString("instances_json_output"));
        // delete the file if it exists
        if (cacheFile.exists()) {
            boolean deleteStatus = cacheFile.delete();
            if (deleteStatus) {
                logger.info("Deleted instances.json");
            } else {
                logger.error("Unable to delete instances.json");
            }
        }

        // calculate the curve
        int maxScore = 0;
        Optional<Instance> maxInstanceScore = instances.stream().max(Comparator.comparingDouble(Instance::getScore));
        if (maxInstanceScore.isPresent()) {
            // make this an int, since we don't care about decimals
            maxScore = (int) maxInstanceScore.get().getScore();
        }
        int curve = 100 - maxScore;

        for (Instance instance : instances) {

            if (instance.getTestResults().isEmpty()) {
                continue;
            }

            // curve the score
            instance.addCurve(curve);

            // write to instances.json file
            cacheArray.put(instance.toJSON());

            // build the score pages
            if (buildWeb) {
                String template = FileUtil.readFile(new File(config.getString("web_path"), "template-score.md"));
                if (template == null) {
                    logger.error("Unable to read template.md! Exiting...");
                    System.exit(1);
                }

                template = template.replace("<api>", instance.getApi());
                template = template.replace("<hash>", instance.getHash());
                template = template.replace("<time>", formattedDate);
                if (instance.getFrontEnd() != null) {
                    String link = "<a href=\"" + instance.getProtocol() + "://" + instance.getFrontEnd() + "\">here</a>.";
                    template = template.replace("<frontend>", "You can use the frontend for this API here: " + link);
                } else {
                    // there's an extra space here on purpose
                    template = template.replace(" <frontend>", "");
                }

                String scoreTable = StringUtil.buildScoreTable(instance);
                template = template.replace("<scores>", scoreTable);
                File scoreFile = new File(config.getString("web_path") + "instance", instance.getHash() + ".md");
                FileUtil.writeFile(template, scoreFile);
            }

        }
        FileUtil.writeFile(cacheArray, cacheFile);

        // write to index.md
        if (buildWeb) {
            String template = FileUtil.readFile(new File(config.getString("web_path"), "template.md"));
            if (template == null) {
                logger.error("Unable to read template.md! Exiting...");
                System.exit(1);
            }
            // create the domain and no domain tables
            String domainTable = StringUtil.buildMainTables(new ArrayList<>(instances), "domain");
            String ipTable = StringUtil.buildMainTables(new ArrayList<>(instances), "ip");
            // replace the placeholder with the tables
            template = template.replace("<main-table>", domainTable);
            template = template.replace("<other-table>", ipTable);
            template = template.replace("<instance-count>", String.valueOf(instances.size()));
            // update the time it was run
            template = template.replace("<time>", formattedDate);
            // write to index.md
            FileUtil.writeFile(template, new File(config.getString("web_path"), "index.md"));
        }
    }

    public static String getCommit() throws IOException, GitAPIException {
        File root = new File("../.git");
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        try (Repository repository = repositoryBuilder.setGitDir(root).readEnvironment().findGitDir().build()) {
            if (repository == null) {
                System.out.println("Not inside a Git repository.");
                return null;
            }
            try (Git git = new Git(repository)) {
                RevCommit latestCommit = git.log().setMaxCount(1).call().iterator().next();
                String fullCommitID = latestCommit.getId().getName();
                return fullCommitID.substring(0, 7);
            }
        }
    }

    private static JSONObject getConfig() {
        File configFile = new File("config.json");
        if (!configFile.exists() || FileUtil.readFile(configFile) == null) {
            logger.error("Config file does not exist! Exiting...");
            return null;
        }
        String contents = FileUtil.readFile(configFile);
        if (contents != null) {
            return new JSONObject(contents);
        } else {
            return null;
        }
    }
}
