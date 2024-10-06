package lol.hyper.cobalttester;

import lol.hyper.cobalttester.instance.Instance;
import lol.hyper.cobalttester.requests.ApiCheck;
import lol.hyper.cobalttester.requests.Test;
import lol.hyper.cobalttester.services.Services;
import lol.hyper.cobalttester.tests.TestBuilder;
import lol.hyper.cobalttester.utils.FileUtil;
import lol.hyper.cobalttester.utils.StringUtil;
import lol.hyper.cobalttester.web.WebBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ReusableMessageFactory;
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
import java.util.concurrent.*;

public class CobaltTester {

    public static Logger logger;
    public static String USER_AGENT = "CobaltTester-git-<commit> (+https://instances.hyper.lol)";
    public static JSONObject config;
    public static final ReusableMessageFactory MESSAGE_FACTORY = new ReusableMessageFactory();

    public static void main(String[] args) {
        long startTime = System.nanoTime();
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
        logger.info("CobaltTester running commit: {}", commit);
        USER_AGENT = USER_AGENT.replace("<commit>", commit);

        boolean buildWeb = false;
        if (args.length != 0) {
            if (args[0].equalsIgnoreCase("web")) {
                buildWeb = true;
            }
        }

        logger.info("Running with args: {}", Arrays.toString(args));

        // Output how many threads we can use
        int availableThreads = Runtime.getRuntime().availableProcessors();
        logger.info("Total available threads: {}", availableThreads);

        // load the config
        config = getConfig();
        if (config == null) {
            logger.error("Unable to load config! Exiting...");
            System.exit(1);
        }

        // load some files
        File instancesFile = new File("instances");
        File testUrlsFile = new File("tests.json");
        File apiKeysFile = new File("apikeys.json");
        List<String> instanceFileContents = FileUtil.readRawFile(instancesFile);
        logger.info("Loaded {} instances from file", instanceFileContents.size());
        String testUrlContents = FileUtil.readFile(testUrlsFile);
        String apiKeyContents = FileUtil.readFile(apiKeysFile);
        if (testUrlContents == null) {
            logger.error("tests.json failed to load!");
            System.exit(1);
        }
        if (apiKeyContents == null) {
            logger.error("apikeys.json failed to load!");
            apiKeyContents = "{}";
        }
        JSONObject testUrlsContents = new JSONObject(testUrlContents);
        JSONObject apiKeys = new JSONObject(apiKeyContents);
        // make sure all files exist
        if (instanceFileContents.isEmpty()) {
            logger.error("Instance file returned empty. Does it exist?");
            System.exit(1);
        }
        if (testUrlsContents.isEmpty()) {
            logger.error("Test URLs file returned empty. Does it exist?");
            System.exit(1);
        }

        // load the tests into services
        Services services = new Services(testUrlsContents);
        services.importTests();

        // shuffle the lists here
        Collections.shuffle(instanceFileContents);

        // load the instance file and build each instance
        List<Instance> instances = new ArrayList<>();
        for (String line : instanceFileContents) {
            // each line is formatted api,frontend,protocol
            // we can split this and get each part
            List<String> lineFix = Arrays.asList(line.split(","));
            String api = lineFix.get(0);
            String frontEnd = lineFix.get(1);
            String protocol = lineFix.get(2);
            String trustStatus;
            if (lineFix.size() == 4) {
                trustStatus = lineFix.get(3);
            } else {
                trustStatus = "unknown";
            }

            // if the instance has "None" set for the frontend
            if (frontEnd.equals("None")) {
                frontEnd = null;
            }
            // build the instance
            logger.info("Setting up instance {}", api);
            Instance newInstance = new Instance(frontEnd, api, protocol, trustStatus);
            newInstance.setHash(StringUtil.makeHash(api));
            instances.add(newInstance);
        }

        // create the tests to make sure each API works
        List<ApiCheck> apiChecks = new ArrayList<>();
        for (Instance instance : instances) {
            ApiCheck apiCheck = new ApiCheck(instance);
            apiChecks.add(apiCheck);
        }

        // create the test builder, which performs the tests
        TestBuilder testBuilder = new TestBuilder();
        // check the APIs to see if they work
        testBuilder.runApiInfoTests(apiChecks);

        // create tests for all APIs that are working
        List<Test> testsToRun = new ArrayList<>();
        for (Instance instance : instances) {
            if (instance.isApiWorking()) {
                String token = null;
                String api = instance.getApi();
                logger.info("{} is ONLINE", instance.getApi());
                if (apiKeys.has(api)) {
                    logger.info("Found authorization token for {}", api);
                    token = apiKeys.getString(api);
                }
                for (Map.Entry<String, String> tests : services.getTests().entrySet()) {
                    String service = tests.getKey();
                    String url = tests.getValue();
                    Test test = new Test(instance, service, url, token);
                    testsToRun.add(test);
                }
                // if the frontend is not null, add it to the tests
                if (instance.getFrontEnd() != null) {
                    Test frontEndTest = new Test(instance, "Frontend", instance.getProtocol() + "://" + instance.getFrontEnd(), null);
                    testsToRun.add(frontEndTest);
                }
            } else {
                logger.info("{} is OFFLINE", instance.getApi());
            }
        }

        // perform the service tests
        Collections.shuffle(testsToRun);
        testBuilder.runServiceTests(testsToRun);

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

        // calculate the scores for all instances
        instances.forEach(Instance::calculateScore);
        // find the highest score
        int highestScore;
        Optional<Instance> maxInstanceScore = instances.stream().max(Comparator.comparingDouble(Instance::getScore));
        highestScore = maxInstanceScore.map(instance -> (int) instance.getScore()).orElse(0);

        for (Instance instance : instances) {
            if (!instance.isApiWorking()) {
                instance.setScore(-1.0);
                cacheArray.put(instance.toJSON());
                continue;
            }

            // curve the score
            instance.addCurve(100 - highestScore);

            // add to instances.json file
            cacheArray.put(instance.toJSON());

            // build the score pages
            if (buildWeb) {
                WebBuilder.buildInstancePage(instance, formattedDate);
            }

        }

        // sort the instances by score
        instances.sort(Comparator.comparingDouble(Instance::getScore).reversed());

        // write instances.json file
        FileUtil.writeFile(cacheArray, cacheFile);

        // write to index.md and make service pages
        if (buildWeb) {
            WebBuilder.buildIndex(instances, formattedDate);
            for (String service : services.getTests().keySet()) {
                String slug = Services.makeSlug(service);
                WebBuilder.buildServicePage(instances, formattedDate, service, slug);
            }
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        long minutesTaken = TimeUnit.MINUTES.convert(duration, TimeUnit.NANOSECONDS);
        logger.info("Completed run in {} minutes.", minutesTaken);
        System.exit(0);
    }

    public static String getCommit() throws IOException, GitAPIException {
        File root = new File("../.git");
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        try (Repository repository = repositoryBuilder.setGitDir(root).readEnvironment().findGitDir().build()) {
            if (repository == null) {
                logger.error("Unable to find git information. Did you fork this correctly?");
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
