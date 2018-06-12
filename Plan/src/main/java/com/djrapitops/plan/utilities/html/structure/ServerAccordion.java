/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.structure;

import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.analysis.MathUtils;
import com.djrapitops.plan.utilities.html.graphs.pie.WorldPie;
import com.djrapitops.plugin.utilities.Format;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * HTML utility class for creating a Server Accordion.
 *
 * @author Rsl1122
 */
public class ServerAccordion extends AbstractAccordion {

    private final StringBuilder viewScript;

    public ServerAccordion(PlayerProfile profile, Map<UUID, String> serverNames) {
        super("server_accordion");

        viewScript = new StringBuilder();

        Map<UUID, WorldTimes> worldTimesPerServer = profile.getWorldTimesPerServer();
        if (worldTimesPerServer.isEmpty()) {
            return;
        }

        addElements(profile, serverNames, worldTimesPerServer);
    }

    public String toViewScript() {
        return viewScript.toString();
    }

    private void addElements(PlayerProfile profile, Map<UUID, String> serverNames, Map<UUID, WorldTimes> worldTimesPerServer) {
        int i = 0;
        for (Map.Entry<UUID, WorldTimes> entry : worldTimesPerServer.entrySet()) {
            UUID serverUUID = entry.getKey();
            String serverName = serverNames.getOrDefault(serverUUID, "Unknown");
            WorldTimes worldTimes = entry.getValue();

            List<Session> sessions = profile.getSessions(serverUUID);
            long playtime = PlayerProfile.getPlaytime(sessions.stream());
            long afkTime = PlayerProfile.getAFKTime(sessions.stream());
            int sessionCount = sessions.size();
            long avgSession = MathUtils.averageLong(playtime, sessionCount);
            long sessionMedian = PlayerProfile.getSessionMedian(sessions.stream());
            long longestSession = PlayerProfile.getLongestSession(sessions.stream());

            long mobKills = PlayerProfile.getMobKillCount(sessions.stream());
            long playerKills = PlayerProfile.getPlayerKills(sessions.stream()).count();
            long deaths = PlayerProfile.getDeathCount(sessions.stream());

            String play = FormatUtils.formatTimeAmount(playtime);
            String afk = FormatUtils.formatTimeAmount(afkTime);
            String avg = sessionCount != 0 ? FormatUtils.formatTimeAmount(avgSession) : "-";
            String median = sessionCount != 0 ? FormatUtils.formatTimeAmount(sessionMedian) : "-";
            String longest = sessionCount != 0 ? FormatUtils.formatTimeAmount(longestSession) : "-";

            String sanitizedServerName = new Format(serverName)
                    .removeSymbols()
                    .removeWhitespace().toString() + i;
            String htmlID = "server_" + sanitizedServerName;

            String worldId = "worldPieServer" + sanitizedServerName;

            WorldPie worldPie = new WorldPie(worldTimes);

            String title = serverName + "<span class=\"pull-right\">" + play + "</span>";

            String leftSide = new AccordionElementContentBuilder()
                    .addRowBold("teal", "calendar-check-o", "Sessions", sessionCount)
                    .addRowBold("green", "clock-o", "Server Playtime", play)
                    .addRowBold("grey", "clock-o", "Time AFK", afk)
                    .addRowBold("teal", "clock-o", "Longest Session", longest)
                    .addRowBold("teal", "clock-o", "Session Median", median)
                    .addBreak()
                    .addRowBold("red", "crosshairs", "Player Kills", playerKills)
                    .addRowBold("green", "crosshairs", "Mob Kills", mobKills)
                    .addRowBold("red", "frown-o", "Deaths", deaths)
                    .toHtml();

            String rightSide = "<div id=\"" + worldId + "\" class=\"dashboard-donut-chart\"></div>" +
                    "<script>" +
                    "var " + worldId + "series = {name:'World Playtime',colorByPoint:true,data:" + worldPie.toHighChartsSeries() + "};" +
                    "var " + worldId + "gmseries = " + worldPie.toHighChartsDrilldown() + ";" +
                    "</script>";

            addElement(new AccordionElement(htmlID, title)
                    .setColor(Theme.getValue(ThemeVal.PARSED_SERVER_ACCORDION))
                    .setLeftSide(leftSide)
                    .setRightSide(rightSide));

            viewScript.append("worldPie(")
                    .append(worldId).append(", ")
                    .append(worldId).append("series, ")
                    .append(worldId).append("gmseries")
                    .append(");");

            i++;
        }
    }
}
