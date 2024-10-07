package lol.hyper.cobalttester.web;

import lol.hyper.cobalttester.CobaltTester;
import lol.hyper.cobalttester.instance.Instance;
import lol.hyper.cobalttester.utils.FileUtil;
import lol.hyper.cobalttester.utils.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WebBuilder {

    private static final Logger logger = LogManager.getLogger(WebBuilder.class);

    public static void buildIndex(List<Instance> instances, String formattedDate) {
        String mainListTemplate = FileUtil.readFile(new File(CobaltTester.config.getString("web_path"), "template-mainlist.md"));
        if (mainListTemplate == null) {
            logger.error("Unable to read template-mainlist.md! Exiting...");
            System.exit(1);
        }
        // create the official, domain, and no domain tables
        String officialTable = StringUtil.buildMainTables(new ArrayList<>(instances), "official");
        String mainTable = StringUtil.buildMainTables(new ArrayList<>(instances), "domain");
        String ipTable = StringUtil.buildMainTables(new ArrayList<>(instances), "ip");
        // replace the placeholder with the tables
        mainListTemplate = mainListTemplate.replaceAll("<main-table-official>", officialTable.replace("sort-table", "official-table"));
        mainListTemplate = mainListTemplate.replaceAll("<main-table-domain>", mainTable.replace("sort-table", "main-table").replace("search", "'main-search'").replace("dropdown", "'main-filter'").replace("slider", "'slider-main'"));
        mainListTemplate = mainListTemplate.replaceAll("<main-table-nodomain>", ipTable.replace("sort-table", "other-table").replace("search", "'other-search'").replace("dropdown", "'other-filter'").replace("slider", "'slider-other'"));
        mainListTemplate = mainListTemplate.replaceAll("<instance-count>", String.valueOf(instances.size()));
        // update the time it was run
        mainListTemplate = mainListTemplate.replaceAll("<time>", formattedDate);
        // write to index.md
        FileUtil.writeFile(mainListTemplate, new File(CobaltTester.config.getString("web_path"), "instances.md"));
    }

    public static void buildInstancePage(Instance instance, String formattedDate) {
        String scoreTemplate = FileUtil.readFile(new File(CobaltTester.config.getString("web_path"), "template-score.md"));
        if (scoreTemplate == null) {
            logger.error("Unable to read template-score.md! Exiting...");
            System.exit(1);
        }

        scoreTemplate = scoreTemplate.replaceAll("<api>", instance.getApi());
        scoreTemplate = scoreTemplate.replaceAll("<hash>", instance.getHash());
        scoreTemplate = scoreTemplate.replaceAll("<time>", formattedDate);
        scoreTemplate = scoreTemplate.replaceAll("<trust>", instance.getTrustStatus());
        scoreTemplate = scoreTemplate.replaceAll("<api-button>", "<a href=\"" + instance.getProtocol() + "://" + instance.getApi() + "\"><button>View API</button></a>");
        if (instance.getFrontEnd() != null) {
            String frontEnd = "<a href=\"" + instance.getProtocol() + "://" + instance.getFrontEnd() + "\"><button>View Frontend</button></a>";
            scoreTemplate = scoreTemplate.replaceAll("<frontend-button>", frontEnd);
        } else {
            scoreTemplate = scoreTemplate.replaceAll("<frontend-button>", "");
        }

        String scoreTable = StringUtil.buildScoreTable(instance);
        // replace the placeholder with the score table
        scoreTemplate = scoreTemplate.replaceAll("<scores>", scoreTable);
        File scoreFile = new File(CobaltTester.config.getString("score_path"), instance.getHash() + ".md");
        FileUtil.writeFile(scoreTemplate, scoreFile);
    }

    public static void buildServicePage(List<Instance> instances, String formattedDate, String service, String slug) {
        // sort into alphabetical order
        Collections.sort(instances);

        String serviceTemplate = FileUtil.readFile(new File(CobaltTester.config.getString("web_path"), "template-service.md"));
        if (serviceTemplate == null) {
            logger.error("Unable to read template-service.md! Exiting...");
            System.exit(1);
        }

        // create the official, domain, and no domain tables
        String officialTable = StringUtil.buildServiceTable(new ArrayList<>(instances), service, "official");
        String mainTable = StringUtil.buildServiceTable(new ArrayList<>(instances), service, "domain");
        String ipTable = StringUtil.buildServiceTable(new ArrayList<>(instances), service, "ip");

        serviceTemplate = serviceTemplate.replaceAll("<time>", formattedDate);
        serviceTemplate = serviceTemplate.replaceAll("<service>", service);
        serviceTemplate = serviceTemplate.replaceAll("<service-slug>", slug);

        // replace the placeholder with the tables
        serviceTemplate = serviceTemplate.replaceAll("<service-table-official>", officialTable.replace("sort-table", "official-table"));
        serviceTemplate = serviceTemplate.replaceAll("<service-table-domain>", mainTable.replace("sort-table", "main-table").replace("search", "'main-search'").replace("dropdown", "'main-filter'").replace("slider", "'slider-main'"));
        serviceTemplate = serviceTemplate.replaceAll("<service-table-nodomain>", ipTable.replace("sort-table", "other-table").replace("search", "'other-search'").replace("dropdown", "'other-filter'").replace("slider", "'slider-other'"));

        FileUtil.writeFile(serviceTemplate, new File(CobaltTester.config.getString("service_path"), slug + ".md"));
    }
}
