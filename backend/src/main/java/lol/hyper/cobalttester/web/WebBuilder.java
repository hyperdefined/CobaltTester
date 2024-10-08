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

    /**
     * Build the main instance page.
     *
     * @param instances     The instances to put on the page.
     * @param formattedDate The date to display.
     */
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
        FileUtil.writeFile(mainListTemplate, new File(CobaltTester.config.getString("web_path"), "index.md"));
    }

    /**
     * Build an instance page for a given instance.
     *
     * @param instance      The instance to make the page for.
     * @param formattedDate The date to display.
     */
    public static void buildInstancePage(Instance instance, String formattedDate) {
        String instanceTemplate = FileUtil.readFile(new File(CobaltTester.config.getString("web_path"), "template-instance.md"));
        if (instanceTemplate == null) {
            logger.error("Unable to read template-instance.md! Exiting...");
            System.exit(1);
        }

        // replace different placeholders with values we want
        instanceTemplate = instanceTemplate.replaceAll("<api>", instance.getApi());
        instanceTemplate = instanceTemplate.replaceAll("<hash>", instance.getHash());
        instanceTemplate = instanceTemplate.replaceAll("<time>", formattedDate);
        instanceTemplate = instanceTemplate.replaceAll("<trust>", instance.getTrustStatus());
        instanceTemplate = instanceTemplate.replaceAll("<api-button>", "<a href=\"" + instance.getProtocol() + "://" + instance.getApi() + "\"><button>View API</button></a>");
        if (instance.getFrontEnd() != null) {
            String frontEnd = "<a href=\"" + instance.getProtocol() + "://" + instance.getFrontEnd() + "\"><button>View Frontend</button></a>";
            instanceTemplate = instanceTemplate.replaceAll("<frontend-button>", frontEnd);
        } else {
            instanceTemplate = instanceTemplate.replaceAll("<frontend-button>", "");
        }

        // create the score table to display the services
        String scoreTable = StringUtil.buildScoreTable(instance);
        // replace the placeholder with the score table
        instanceTemplate = instanceTemplate.replaceAll("<scores>", scoreTable);
        File scoreFile = new File(CobaltTester.config.getString("score_path"), instance.getHash() + ".md");
        FileUtil.writeFile(instanceTemplate, scoreFile);
    }

    /**
     * Build the page of instances for a given service.
     *
     * @param instances     The instances to put on the table.
     * @param formattedDate The date to display.
     * @param service       The service friendly name.
     * @param slug          The slug for the URL.
     */
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
