/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.utilities.html.structure;

import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.containers.PerServerContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PerServerKeys;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.graphs.Graphs;
import com.djrapitops.plan.utilities.html.graphs.pie.WorldPie;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.plan.utilities.html.icon.Icons;
import com.djrapitops.plugin.utilities.Format;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * HTML utility class for creating a Server Accordion.
 *
 * @author Rsl1122
 */
public class ServerAccordion extends Accordion {

    private final StringBuilder viewScript;

    private final Map<UUID, String> serverNames;
    private PerServerContainer perServer;

    private final Theme theme;
    private final Graphs graphs;
    private final Formatter<Long> yearLongFormatter;
    private final Formatter<Long> timeAmountFormatter;

    public ServerAccordion(
            PlayerContainer container, Map<UUID, String> serverNames,
            Theme theme,
            Graphs graphs,
            Formatter<Long> yearLongFormatter,
            Formatter<Long> timeAmountFormatter
    ) {
        super("server_accordion");
        this.theme = theme;
        this.graphs = graphs;
        this.yearLongFormatter = yearLongFormatter;
        this.timeAmountFormatter = timeAmountFormatter;

        viewScript = new StringBuilder();

        this.serverNames = serverNames;
        Optional<PerServerContainer> perServerData = container.getValue(PlayerKeys.PER_SERVER);
        if (perServerData.isPresent()) {
            perServer = perServerData.get();
        } else {
            return;
        }

        addElements();
    }

    public String toViewScript() {
        return viewScript.toString();
    }

    private void addElements() {
        int i = 0;

        for (Map.Entry<UUID, DataContainer> entry : perServer.entrySet()) {
            UUID serverUUID = entry.getKey();
            DataContainer container = entry.getValue();
            String serverName = serverNames.getOrDefault(serverUUID, "Unknown");
            WorldTimes worldTimes = container.getValue(PerServerKeys.WORLD_TIMES).orElse(new WorldTimes(new HashMap<>()));
            SessionsMutator sessionsMutator = SessionsMutator.forContainer(container);

            boolean banned = container.getValue(PerServerKeys.BANNED).orElse(false);
            boolean operator = container.getValue(PerServerKeys.OPERATOR).orElse(false);
            long registered = container.getValue(PerServerKeys.REGISTERED).orElse(0L);

            long playtime = sessionsMutator.toPlaytime();
            long afkTime = sessionsMutator.toAfkTime();
            int sessionCount = sessionsMutator.count();
            long sessionMedian = sessionsMutator.toMedianSessionLength();
            long longestSession = sessionsMutator.toLongestSessionLength();

            long mobKills = sessionsMutator.toMobKillCount();
            long playerKills = sessionsMutator.toPlayerKillCount();
            long deaths = sessionsMutator.toDeathCount();

            String play = timeAmountFormatter.apply(playtime);
            String afk = timeAmountFormatter.apply(afkTime);
            String median = timeAmountFormatter.apply(sessionMedian);
            String longest = timeAmountFormatter.apply(longestSession);

            String sanitizedServerName = new Format(serverName)
                    .removeSymbols()
                    .removeWhitespace().toString() + i;
            String htmlID = "server_" + sanitizedServerName;

            String worldId = "worldPieServer" + sanitizedServerName;

            WorldPie worldPie = graphs.pie().worldPie(worldTimes);

            String title = serverName + "<span class=\"pull-right\">" + play + "</span>";

            String leftSide = new AccordionElementContentBuilder()
                    .addRowBold(Icons.OPERATOR, "Operator", operator ? "Yes" : "No")
                    .addRowBold(Icons.BANNED, "Banned", banned ? "Yes" : "No")
                    .addRowBold(Icon.called("user-plus").of(Color.LIGHT_GREEN), "Registered", yearLongFormatter.apply(registered))
                    .addBreak()
                    .addRowBold(Icons.SESSION_COUNT, "Sessions", sessionCount)
                    .addRowBold(Icons.PLAYTIME, "Server Playtime", play)
                    .addRowBold(Icons.AFK_LENGTH, "Time AFK", afk)
                    .addRowBold(Icons.SESSION_LENGTH, "Longest Session", longest)
                    .addRowBold(Icons.SESSION_LENGTH, "Session Median", median)
                    .addBreak()
                    .addRowBold(Icons.PLAYER_KILLS, "Player Kills", playerKills)
                    .addRowBold(Icons.MOB_KILLS, "Mob Kills", mobKills)
                    .addRowBold(Icons.DEATHS, "Deaths", deaths)
                    .toHtml();

            String rightSide = "<div id=\"" + worldId + "\" class=\"dashboard-donut-chart\"></div>" +
                    "<script>" +
                    "var " + worldId + "series = {name:'World Playtime',colorByPoint:true,data:" + worldPie.toHighChartsSeries() + "};" +
                    "var " + worldId + "gmseries = " + worldPie.toHighChartsDrilldown() + ";" +
                    "</script>";

            addElement(new AccordionElement(htmlID, title)
                    .setColor(theme.getValue(ThemeVal.PARSED_SERVER_ACCORDION))
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
