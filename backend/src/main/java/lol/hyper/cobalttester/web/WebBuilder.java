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
        mainListTemplate = mainListTemplate.replaceAll("<main-table-official>", officialTable);
        mainListTemplate = mainListTemplate.replaceAll("<main-table-domain>", mainTable);
        mainListTemplate = mainListTemplate.replaceAll("<main-table-nodomain>", ipTable);
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
        if (instance.getFrontEnd() != null) {
            String link = "<a href=\"" + instance.getProtocol() + "://" + instance.getFrontEnd() + "\">here</a>.";
            scoreTemplate = scoreTemplate.replaceAll("<frontend>", "You can use the frontend for this API here: " + link);
        } else {
            // there's an extra space here on purpose
            scoreTemplate = scoreTemplate.replaceAll(" <frontend>", "");
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
        serviceTemplate = serviceTemplate.replaceAll("<service-table-official>", officialTable);
        serviceTemplate = serviceTemplate.replaceAll("<service-table-domain>", mainTable);
        serviceTemplate = serviceTemplate.replaceAll("<service-table-nodomain>", ipTable);

        FileUtil.writeFile(serviceTemplate, new File(CobaltTester.config.getString("service_path"), slug + ".md"));
    }
}
