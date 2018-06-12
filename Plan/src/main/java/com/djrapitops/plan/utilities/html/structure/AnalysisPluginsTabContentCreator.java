/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.structure;

import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.analysis.Analysis;
import com.djrapitops.plan.utilities.comparators.PluginDataNameComparator;
import com.djrapitops.plan.utilities.html.tables.PluginPlayersTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Creates Plugin section contents for Analysis page.
 *
 * @author Rsl1122
 */
public class AnalysisPluginsTabContentCreator {

    public static String[] createContent(Map<PluginData, AnalysisContainer> containers) {
        if (containers.isEmpty()) {
            return new String[]{"<li><a>No Data</a></li>", ""};
        }

        List<PluginData> order = new ArrayList<>(containers.keySet());
        order.sort(new PluginDataNameComparator());

        StringBuilder nav = new StringBuilder();
        StringBuilder generalTab = new StringBuilder();
        StringBuilder otherTabs = new StringBuilder();

        generalTab.append("<div class=\"tab\"><div class=\"row clearfix\">");

        boolean displayGeneralTab = false;

        for (PluginData pluginData : order) {
            AnalysisContainer container = containers.get(pluginData);

            switch (pluginData.getSize()) {
                case TAB:
                    appendNewTab(pluginData, container, nav, otherTabs);
                    break;
                case WHOLE:
                    if (!container.hasOnlyValues()) {
                        appendWhole(pluginData, container, generalTab);
                        displayGeneralTab = true;
                    }
                    break;
                case TWO_THIRDS:
                    if (!container.hasOnlyValues()) {
                        appendTwoThirds(pluginData, container, generalTab);
                        displayGeneralTab = true;
                    }
                    break;
                case THIRD:
                default:
                    appendThird(pluginData, container, generalTab);
                    displayGeneralTab = true;
                    break;
            }
        }

        generalTab.append("</div></div>");

        String playerListTab = "<div class=\"tab\">" +
                "<div class=\"row clearfix\">" +
                "<div class=\"col-lg-12 col-md-12 col-sm-12 col-xs-12\">" +
                "<div class=\"card\">" +
                "<div class=\"header\"><h2><i class=\"fa fa-users\"></i> Plugin Data</h2></div>" +
                "<div class=\"body\">" +
                new PluginPlayersTable(containers, Analysis.getServerProfile().getPlayers()).parseHtml() +
                "</div></div></div>" +
                "</div></div></div>";

        return new String[]{
                (displayGeneralTab ? "<li><a class=\"nav-button\" href=\"javascript:void(0)\">General</a></li>" : "")
                        + "<li><a class=\"nav-button\" href=\"javascript:void(0)\">Player Data</a></li>" + nav.toString(),
                (displayGeneralTab ? generalTab.toString() : "") + playerListTab + otherTabs.toString()
        };
    }

    public static void appendThird(PluginData pluginData, InspectContainer container, StringBuilder generalTab) {
        generalTab.append("<div class=\"col-xs-12 col-sm-12 col-md-4 col-lg-4\">" +
                "<div class=\"card\">" +
                "<div class=\"header\">" +
                "<h2>")
                .append(pluginData.parsePluginIcon()).append(" ").append(pluginData.getSourcePlugin())
                .append("</h2></div>")
                .append(container.parseHtml())
                .append("</div></div>");
    }

    private static void appendTwoThirds(PluginData pluginData, AnalysisContainer container, StringBuilder generalTab) {
        generalTab.append("<div class=\"col-xs-12 col-sm-12 col-md-8 col-lg-8\">" +
                "<div class=\"card\">" +
                "<div class=\"header\">" +
                "<h2>")
                .append(pluginData.parsePluginIcon()).append(" ").append(pluginData.getSourcePlugin())
                .append("</h2></div>")
                .append(container.parseHtml())
                .append("</div></div>");
    }

    private static void appendWhole(PluginData pluginData, AnalysisContainer container, StringBuilder generalTab) {
        generalTab.append("<div class=\"col-xs-12 col-sm-12 col-md-12 col-lg-12\">" +
                "<div class=\"card\">" +
                "<div class=\"header\">" +
                "<h2>")
                .append(pluginData.parsePluginIcon()).append(" ").append(pluginData.getSourcePlugin())
                .append("</h2></div>").append("<div class=\"body\">")
                .append(container.parseHtml())
                .append("</div></div></div>");
    }

    private static void appendNewTab(PluginData pluginData, AnalysisContainer container, StringBuilder nav, StringBuilder otherTabs) {
        nav.append("<li><a class=\"nav-button\" href=\"javascript:void(0)\">").append(pluginData.getSourcePlugin()).append("</a></li>");
        otherTabs.append("<div class=\"tab\"><div class=\"row clearfix\"><div class=\"col-xs-12 col-sm-12 col-md-12 col-lg-12\">" +
                "<div class=\"card\">" +
                "<div class=\"header\">" +
                "<h2>")
                .append(pluginData.parsePluginIcon()).append(" ").append(pluginData.getSourcePlugin())
                .append("</h2></div>")
                .append(container.parseHtml())
                .append("</div></div></div></div>");
    }
}
