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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CobaltTester {

    public static Logger logger;
    public static String USER_AGENT;
    public static JSONObject config;
    public static final ReusableMessageFactory MESSAGE_FACTORY = new ReusableMessageFactory();

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        System.setProperty("log4j.configurationFile", "log4j2config.xml");
        logger = LogManager.getLogger(CobaltTester.class);
        logger.info("Running with args: {}", Arrays.toString(args));

        Init init = new Init();
        init.start(args);

        // load the config
        config = init.getConfig();
        if (config == null) {
            logger.error("Unable to load config! Exiting...");
            System.exit(1);
        }

        // set the user agent
        USER_AGENT = init.getUserAgent();

        // load the tests into services
        Services services = new Services(init.getTests());
        services.importTests();

        // shuffle the lists here
        Collections.shuffle(init.getInstanceFileContents());

        // load the instance file and build each instance
        List<Instance> instances = new ArrayList<>();
        for (String line : init.getInstanceFileContents()) {
            // each line is formatted api,frontend,protocol
            // we can split this and get each part
            List<String> lineFix = Arrays.asList(line.split(","));
            String api = lineFix.get(0);
            String frontEnd = lineFix.get(1);
            String protocol = lineFix.get(2);
            String trustStatus;
            // if there is no trust level listed, assume to be unknown
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
            // only create tests if the API is working
            if (instance.isApiWorking()) {
                String token = null;
                String api = instance.getApi();
                logger.info("{} is ONLINE", instance.getApi());
                // if we have an API key for this instance, use it for tests
                if (init.getApiKeys().has(api)) {
                    logger.info("Found API key for {}, will use it for requests", api);
                    token = init.getApiKeys().getString(api);
                }
                // create the tests for each service for this instance
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

        // set when the tests ran. this will be afterward, as it's more for "as of this time"
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedDate = f.format(new Date());

        // remove the old instance.json file
        JSONArray instancesOutput = new JSONArray();
        File instancesOutputFile = new File(config.getString("instances_json_output"));
        // delete the file if it exists
        if (instancesOutputFile.exists()) {
            boolean deleteStatus = instancesOutputFile.delete();
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
                instancesOutput.put(instance.toJSON());
                continue;
            }

            // curve the score
            instance.addCurve(100 - highestScore);

            // add to instances.json file
            instancesOutput.put(instance.toJSON());

            // if web build is enabled, write each instance page
            if (init.buildWeb()) {
                WebBuilder.buildInstancePage(instance, formattedDate);
            }
        }

        // sort the instances by score
        instances.sort(Comparator.comparingDouble(Instance::getScore).reversed());

        // write instances.json file
        FileUtil.writeFile(instancesOutput, instancesOutputFile);

        // if web build is enabled, write index and service pages
        if (init.buildWeb()) {
            WebBuilder.buildIndex(instances, formattedDate);
            for (String service : services.getTests().keySet()) {
                String slug = Services.makeSlug(service);
                WebBuilder.buildServicePage(instances, formattedDate, service, slug);
            }
        }

        // display how long the test took
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        long minutesTaken = TimeUnit.MINUTES.convert(duration, TimeUnit.NANOSECONDS);
        logger.info("Completed run in {} minutes.", minutesTaken);
        System.exit(0);
    }
}
