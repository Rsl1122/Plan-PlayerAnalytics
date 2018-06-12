/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.info.server.ServerProperties;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.analysis.Analysis;
import com.djrapitops.plan.utilities.html.graphs.line.OnlineActivityGraph;
import com.djrapitops.plan.utilities.html.structure.SessionTabStructureCreator;
import com.djrapitops.plan.utilities.html.tables.SessionsTableCreator;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class for parsing layout components of the websites.
 *
 * @author Rsl1122
 */
public class HtmlStructure {

    public static String separateWithDots(String... elements) {
        if (elements.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            String element = elements[i];
            if (element.isEmpty()) {
                continue;
            }
            builder.append(" &#x2022; ");
            builder.append(element);
        }
        return builder.toString();
    }

    public static String createDotList(String... elements) {
        if (elements.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String element : elements) {
            if (element.isEmpty()) {
                continue;
            }
            builder.append("&#x2022; ");
            builder.append(element);
            builder.append("<br>");
        }
        return builder.toString();
    }

    public static String[] createSessionsTabContentInspectPage(Map<String, List<Session>> sessions, List<Session> allSessions, UUID uuid) {
        if (Settings.DISPLAY_SESSIONS_AS_TABLE.isTrue()) {
            Map<UUID, List<Session>> sessionsByPlayer = new HashMap<>();
            sessionsByPlayer.put(uuid, allSessions);
            return new String[]{Html.TABLE_SESSIONS.parse("", "", "", SessionsTableCreator.createTable(sessionsByPlayer, allSessions)[0]), ""};
        } else {
            Map<UUID, Map<String, List<Session>>> map = new HashMap<>();
            map.put(uuid, sessions);
            return SessionTabStructureCreator.createStructure(map, allSessions, false);
        }
    }

    public static String[] createInspectPageTabContentCalculating() {
        String tab = "<div class=\"tab\">" +
                "<div class=\"row clearfix\">" +
                "<div class=\"col-lg-12 col-md-12 col-sm-12 col-xs-12\">" +
                "<div class=\"card\">" +
                "<div class=\"header\"><h2><i class=\"fa fa-users\"></i> Plugin Data</h2></div>" +
                "<div class=\"body\">" +
                "<p><i class=\"fa fa-spin fa-refresh\"></i> Calculating Plugins tab, refresh (F5) shortly..</p>" +
                "</div></div>" +
                "</div></div></div>";
        return new String[]{"<li><a>Calculating... Refresh shortly</a></li>", tab};
    }

    public static String createNetworkPageContent(Map<UUID, String> networkPageContents) {
        if (Verify.isEmpty(networkPageContents)) {
            return "";
        }
        int i = 0;
        StringBuilder b = new StringBuilder();
        List<String> values = new ArrayList<>(networkPageContents.values());
        Collections.sort(values);
        int size = values.size();
        for (String server : values) {
            if (i % 2 == 0) {
                b.append("<div class=\"row clearfix\">");
            }
            b.append(server);
            if ((i + 1) % 2 == 0 || i + 1 == size) {
                b.append("</div>");
            }
            i++;
        }
        return b.toString();
    }

    public static String createServerContainer() {
        ServerProperties properties = ServerInfo.getServerProperties();
        int maxPlayers = properties.getMaxPlayers();
        int online = properties.getOnlinePlayers();
        Optional<Long> analysisRefreshDate = Analysis.getRefreshDate();
        String refresh = analysisRefreshDate.map(FormatUtils::formatTimeStampClock).orElse("-");

        Server server = ServerInfo.getServer();

        String serverName = server.getName();
        String serverType = properties.getVersion();
        String address = "../server/" + serverName;

        Database db = Database.getActive();
        UUID serverUUID = server.getUuid();
        String id = ThreadLocalRandom.current().nextInt(100) + serverUUID.toString().replace("-", "");

        int playerCount = 0;
        String playerData = "[]";
        try {
            playerCount = db.count().getServerPlayerCount(serverUUID);
            playerData = new OnlineActivityGraph(db.fetch().getTPSData(serverUUID)).toHighChartsSeries();
        } catch (DBException e) {
            Log.toLog(HtmlStructure.class.getClass().getName(), e);
        }

        return "<div class=\"col-xs-12 col-sm-12 col-md-6 col-lg-6\">" +
                "<div class=\"card\">" +
                "<div class=\"header\">" +
                "<div class=\"row clearfix\">" +
                "<div class=\"col-xs-12 col-sm-12\">" +
                "<h2><i class=\"col-light-green fa fa-server\"></i> " + serverName + "</h2>" +
                "</div></div></div>" +
                "<div class=\"panel panel-default\">" +
                "<div class=\"panel-heading\">" +
                "<div class=\"row\">" +
                "<div id=\"playerChart" + id + "\" style=\"width: 100%; height: 300px;\"></div>" +
                "</div></div>" +
                "<div class=\"panel-body\">" +
                "<div class=\"row\">" +
                "<div class=\"col-md-8\">" +
                "<p><i class=\"fa fa-users\"></i> Registered Players " +
                "<span class=\"pull-right\">" + playerCount + "</span></p>" +
                "<p><i class=\"col-blue fa fa-user\"></i> Players Online " +
                "<span class=\"pull-right\">" + online + " / " + maxPlayers + "</span></p>" +
                "<p><i class=\"col-deep-orange fa fa-compass\"></i> Type " +
                "<span class=\"pull-right\">" + serverType + "</span></p></div>" +
                "<div class=\"col-md-4\">" +
                "<p><i class=\"fa fa-clock-o\"></i> Last Refresh" +
                "<span class=\"pull-right\"><b>" + refresh + "</b></span></p>" +
                "<br>" +
                "<a href=\"" + address + "\"><button href=\"" + address + "\" type=\"button\" class=\"pull-right btn bg-light-green waves-effect\">" +
                "<i class=\"material-icons\">trending_up</i>" +
                "<span>ANALYSIS</span>" +
                "</button></a></div></div></div></div></div></div>" +
                "<script>" +
                "var playersOnlineSeries" + id + " = {" +
                "name: 'Players Online'," +
                "data: " + playerData + "," +
                "type: 'areaspline'," +
                "color: '${playersGraphColor}'," +
                "tooltip: {" +
                "valueDecimals: 0" +
                "}};" +
                "</script>" +
                "<script>$(function () {setTimeout(" +
                "function() {" +
                "playersChartNoNav(playerChart" + id + ", playersOnlineSeries" + id + ");}, 1000);" +
                "})</script>";
    }

    public static String playerStatus(String online, Set<UUID> banned, boolean op) {
        boolean offline = "offline".equalsIgnoreCase(online);

        StringBuilder html = new StringBuilder("<p>");
        if (offline) {
            html.append(Html.FA_COLORED_ICON.parse("red", "circle")).append(" ").append(online);
        } else {
            html.append(Html.FA_COLORED_ICON.parse("green", "circle")).append(" Online (").append(online).append(")");
        }
        html.append("</p>");
        if (op) {
            html.append("<p>").append(Html.FA_COLORED_ICON.parse("blue", "superpowers")).append(" Operator</p>");
        }
        int bannedOn = banned.size();
        if (bannedOn != 0) {
            html.append("<p>").append(Html.FA_COLORED_ICON.parse("red", "gavel")).append(" Banned (").append(bannedOn).append(")");
        }
        return html.toString();
    }
}
