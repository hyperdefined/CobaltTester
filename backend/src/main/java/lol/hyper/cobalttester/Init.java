package lol.hyper.cobalttester;

import lol.hyper.cobalttester.utils.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class Init {

    public Logger logger = LogManager.getLogger(this);
    private String userAgent;
    private boolean buildWeb = false;
    private JSONObject config;
    private JSONObject tests;
    private JSONObject apiKeys;
    private List<String> instanceFileContents;

    public void start(String[] args) {
        // load the git information
        String commit = null;
        try {
            commit = getCommit();
        } catch (IOException | GitAPIException exception) {
            logger.error("Unable to get git repo information! Did you clone this correctly?", exception);
            System.exit(1);
        }

        if (commit == null) {
            logger.error("Unable to get git repo information! Did you clone this correctly?");
            System.exit(1);
        }

        // set up the user agent
        logger.info("CobaltTester starting up.");
        logger.info("CobaltTester running commit: {}", commit);
        userAgent = "CobaltTester-git-<commit> (+https://instances.hyper.lol)".replace("<commit>", commit);

        // if the user wants to build web
        if (args.length != 0) {
            if (args[0].equalsIgnoreCase("web")) {
                buildWeb = true;
            }
        }

        // Output how many threads we can use
        int availableThreads = Runtime.getRuntime().availableProcessors();
        logger.info("Total available threads: {}", availableThreads);

        // load the config
        loadConfig();

        // load files
        setupFiles();
    }

    public JSONObject getConfig() {
        return config;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public boolean buildWeb() {
        return buildWeb;
    }

    public JSONObject getApiKeys() {
        return apiKeys;
    }

    public JSONObject getTests() {
        return tests;
    }

    public List<String> getInstanceFileContents() {
        return instanceFileContents;
    }

    /**
     * Gets the commit this is running.
     *
     * @return The commit hash, down to the first 6 characters.
     * @throws IOException     If there were any IO issues.
     * @throws GitAPIException If there were any issues reading the git information.
     */
    private String getCommit() throws IOException, GitAPIException {
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

    /**
     * Loads the config from config.json.
     */
    private void loadConfig() {
        File configFile = new File("config.json");
        if (!configFile.exists() || FileUtil.readFile(configFile) == null) {
            config = new JSONObject();
            config.put("web_path", "../web/");
            config.put("score_path", "../web/instance");
            config.put("service_path", "../web/service");
            config.put("instances_json_output", "instances.json");
            logger.warn("Config file does not exist! Creating default...");
            FileUtil.writeFile(config.toString(4), new File("config.json"));
        }
        String contents = FileUtil.readFile(configFile);
        if (contents == null) {
            logger.error("config.json exists, but unable to read contents of it!");
            config = null;
            return;
        }
        config = new JSONObject(contents);
    }

    private void setupFiles() {
        // load some files
        File instancesFile = new File("instances");
        File testUrlsFile = new File("tests.json");
        File apiKeysFile = new File("apikeys.json");
        instanceFileContents = FileUtil.readRawFile(instancesFile);
        if (instanceFileContents.isEmpty()) {
            logger.error("Instance file returned empty. Does it exist?");
            System.exit(1);
        }
        String testUrlContents = FileUtil.readFile(testUrlsFile);
        String apiKeyContents = FileUtil.readFile(apiKeysFile);
        if (testUrlContents == null) {
            logger.error("tests.json failed to load! Unable to continue. Make sure tests.json exists!");
            System.exit(1);
        }
        if (apiKeyContents == null) {
            logger.info("apikeys.json is missing, making example file");
            JSONObject exampleApiKeys = new JSONObject();
            exampleApiKeys.put("api.domain.com", "api-key");
            FileUtil.writeFile(exampleApiKeys.toString(4), new File("apikeys.json"));
            apiKeyContents = "{}";
        }
        tests = new JSONObject(testUrlContents);
        if (tests.isEmpty()) {
            logger.error("tests.json exists, but it's empty?");
            System.exit(1);
        }
        apiKeys = new JSONObject(apiKeyContents);

        // folders for web
        if (buildWeb) {
            File serviceFolder = new File("../web", "service");
            File instanceFolder = new File("../web", "instance");
            try {
                Files.createDirectory(serviceFolder.toPath());
                logger.info("Creating folder for web: {}", serviceFolder.getAbsolutePath());
                Files.createDirectory(instanceFolder.toPath());
                logger.info("Creating folder for web: {}", instanceFolder.getAbsolutePath());
            } catch (IOException exception) {
                logger.error("Unable to make folder for web!", exception);
            }
        }
    }
}
