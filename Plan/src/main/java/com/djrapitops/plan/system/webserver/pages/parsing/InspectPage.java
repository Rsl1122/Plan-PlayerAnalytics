/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.pages.parsing;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plan.data.calculation.ActivityIndex;
import com.djrapitops.plan.data.container.Action;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.analysis.MathUtils;
import com.djrapitops.plan.utilities.comparators.SessionLengthComparator;
import com.djrapitops.plan.utilities.comparators.SessionStartComparator;
import com.djrapitops.plan.utilities.file.FileUtil;
import com.djrapitops.plan.utilities.html.HtmlStructure;
import com.djrapitops.plan.utilities.html.HtmlUtils;
import com.djrapitops.plan.utilities.html.graphs.PunchCardGraph;
import com.djrapitops.plan.utilities.html.graphs.calendar.PlayerCalendar;
import com.djrapitops.plan.utilities.html.graphs.pie.ServerPreferencePie;
import com.djrapitops.plan.utilities.html.graphs.pie.WorldPie;
import com.djrapitops.plan.utilities.html.structure.ServerAccordion;
import com.djrapitops.plan.utilities.html.tables.ActionsTable;
import com.djrapitops.plan.utilities.html.tables.GeoInfoTable;
import com.djrapitops.plan.utilities.html.tables.NicknameTable;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.TimeAmount;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Used for parsing Inspect page out of database data and the html.
 *
 * @author Rsl1122
 */
public class InspectPage extends Page {

    private final UUID uuid;

    public InspectPage(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toHtml() throws ParseException {
        try {
            if (uuid == null) {
                throw new IllegalStateException("UUID was null!");
            }
            Benchmark.start("Inspect Parse, Fetch");
            Database db = Database.getActive();
            PlayerProfile profile = db.fetch().getPlayerProfile(uuid);
            if (profile == null) {
                throw new IllegalStateException("Player profile was null!");
            }
            UUID serverUUID = ServerInfo.getServerUUID();
            Map<UUID, String> serverNames = db.fetch().getServerNames();

            Benchmark.stop("Inspect Parse, Fetch");

            return parse(profile, serverUUID, serverNames);
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }

    public String parse(PlayerProfile profile, UUID serverUUID, Map<UUID, String> serverNames) throws IOException {
        long now = System.currentTimeMillis();

        addValue("refresh", FormatUtils.formatTimeStampClock(now));
        addValue("version", MiscUtils.getPlanVersion());
        addValue("timeZone", MiscUtils.getTimeZoneOffsetHours());

        String online = "Offline";
        Optional<Session> activeSession = SessionCache.getCachedSession(uuid);
        if (activeSession.isPresent()) {
            Session session = activeSession.get();
            session.setSessionID(Integer.MAX_VALUE);
            profile.addActiveSession(session);
            online = serverNames.get(serverUUID);
        }

        String playerName = profile.getName();
        long registered = profile.getRegistered();
        int timesKicked = profile.getTimesKicked();
        long lastSeen = profile.getLastSeen();

        addValue("registered", FormatUtils.formatTimeStampYear(registered));
        addValue("playerName", playerName);
        addValue("kickCount", timesKicked);

        addValue("lastSeen", lastSeen != 0 ? FormatUtils.formatTimeStampYear(lastSeen) : "-");

        Map<UUID, WorldTimes> worldTimesPerServer = profile.getWorldTimesPerServer();
        addValue("serverPieSeries", new ServerPreferencePie(serverNames, worldTimesPerServer).toHighChartsSeries());
        addValue("worldPieColors", Theme.getValue(ThemeVal.GRAPH_WORLD_PIE));
        addValue("gmPieColors", Theme.getValue(ThemeVal.GRAPH_GM_PIE));
        addValue("serverPieColors", Theme.getValue(ThemeVal.GRAPH_SERVER_PREF_PIE));

        String favoriteServer = serverNames.get(profile.getFavoriteServer());
        addValue("favoriteServer", favoriteServer != null ? favoriteServer : "Unknown");

        addValue("tableBodyNicknames", new NicknameTable(profile.getNicknames(), serverNames).parseBody());
        addValue("tableBodyIPs", new GeoInfoTable(profile.getGeoInformation()).parseBody());

        Map<UUID, List<Session>> sessions = profile.getSessions();
        Map<String, List<Session>> sessionsByServerName = sessions.entrySet().stream()
                .collect(Collectors.toMap(entry -> serverNames.get(entry.getKey()), Map.Entry::getValue));

        List<Session> allSessions = profile.getAllSessions()
                .sorted(new SessionStartComparator())
                .collect(Collectors.toList());

        String[] sessionsAccordion = HtmlStructure.createSessionsTabContentInspectPage(sessionsByServerName, allSessions, uuid);

        ServerAccordion serverAccordion = new ServerAccordion(profile, serverNames);

        PlayerCalendar playerCalendar = new PlayerCalendar(allSessions, registered);

        addValue("calendarSeries", playerCalendar.toCalendarSeries());
        addValue("firstDay", 1);

        addValue("accordionSessions", sessionsAccordion[0]);
        addValue("accordionServers", serverAccordion.toHtml());
        addValue("sessionTabGraphViewFunctions", sessionsAccordion[1] + serverAccordion.toViewScript());

        long dayAgo = now - TimeAmount.DAY.ms();
        long weekAgo = now - TimeAmount.WEEK.ms();
        long monthAgo = now - TimeAmount.MONTH.ms();

        List<Session> sessionsDay = profile.getSessions(dayAgo, now).collect(Collectors.toList());
        List<Session> sessionsWeek = profile.getSessions(weekAgo, now).collect(Collectors.toList());
        List<Session> sessionsMonth = profile.getSessions(monthAgo, now).collect(Collectors.toList());

        long playtime = PlayerProfile.getPlaytime(allSessions.stream());
        long playtimeDay = PlayerProfile.getPlaytime(sessionsDay.stream());
        long playtimeWeek = PlayerProfile.getPlaytime(sessionsWeek.stream());
        long playtimeMonth = PlayerProfile.getPlaytime(sessionsMonth.stream());

        long afk = PlayerProfile.getAFKTime(allSessions.stream());
        long afkDay = PlayerProfile.getAFKTime(sessionsDay.stream());
        long afkWeek = PlayerProfile.getAFKTime(sessionsWeek.stream());
        long afkMonth = PlayerProfile.getAFKTime(sessionsMonth.stream());

        long activeTotal = playtime - afk;

        long longestSession = PlayerProfile.getLongestSession(allSessions.stream());
        long longestSessionDay = PlayerProfile.getLongestSession(sessionsDay.stream());
        long longestSessionWeek = PlayerProfile.getLongestSession(sessionsWeek.stream());
        long longestSessionMonth = PlayerProfile.getLongestSession(sessionsMonth.stream());

        long sessionMedian = PlayerProfile.getSessionMedian(allSessions.stream());
        long sessionMedianDay = PlayerProfile.getSessionMedian(sessionsDay.stream());
        long sessionMedianWeek = PlayerProfile.getSessionMedian(sessionsWeek.stream());
        long sessionMedianMonth = PlayerProfile.getSessionMedian(sessionsMonth.stream());

        int sessionCount = allSessions.size();
        int sessionCountDay = sessionsDay.size();
        int sessionCountWeek = sessionsWeek.size();
        int sessionCountMonth = sessionsMonth.size();

        long sessionAverage = MathUtils.averageLong(playtime, sessionCount);
        long sessionAverageDay = MathUtils.averageLong(playtimeDay, sessionCountDay);
        long sessionAverageWeek = MathUtils.averageLong(playtimeWeek, sessionCountWeek);
        long sessionAverageMonth = MathUtils.averageLong(playtimeMonth, sessionCountMonth);

        addValue("playtimeTotal", playtime > 0L ? FormatUtils.formatTimeAmount(playtime) : "-");
        addValue("playtimeDay", playtimeDay > 0L ? FormatUtils.formatTimeAmount(playtimeDay) : "-");
        addValue("playtimeWeek", playtimeWeek > 0L ? FormatUtils.formatTimeAmount(playtimeWeek) : "-");
        addValue("playtimeMonth", playtimeMonth > 0L ? FormatUtils.formatTimeAmount(playtimeMonth) : "-");

        addValue("activeTotal", activeTotal > 0L ? FormatUtils.formatTimeAmount(activeTotal) : "-");

        addValue("afkTotal", afk > 0L ? FormatUtils.formatTimeAmount(afk) : "-");
        addValue("afkDay", afkDay > 0L ? FormatUtils.formatTimeAmount(afkDay) : "-");
        addValue("afkWeek", afkWeek > 0L ? FormatUtils.formatTimeAmount(afkWeek) : "-");
        addValue("afkMonth", afkMonth > 0L ? FormatUtils.formatTimeAmount(afkMonth) : "-");

        addValue("sessionLengthLongest", longestSession > 0L ? FormatUtils.formatTimeAmount(longestSession) : "-");
        addValue("sessionLongestDay", longestSessionDay > 0L ? FormatUtils.formatTimeAmount(longestSessionDay) : "-");
        addValue("sessionLongestWeek", longestSessionWeek > 0L ? FormatUtils.formatTimeAmount(longestSessionWeek) : "-");
        addValue("sessionLongestMonth", longestSessionMonth > 0L ? FormatUtils.formatTimeAmount(longestSessionMonth) : "-");

        addValue("sessionLengthMedian", sessionMedian > 0L ? FormatUtils.formatTimeAmount(sessionMedian) : "-");
        addValue("sessionMedianDay", sessionMedianDay > 0L ? FormatUtils.formatTimeAmount(sessionMedianDay) : "-");
        addValue("sessionMedianWeek", sessionMedianWeek > 0L ? FormatUtils.formatTimeAmount(sessionMedianWeek) : "-");
        addValue("sessionMedianMonth", sessionMedianMonth > 0L ? FormatUtils.formatTimeAmount(sessionMedianMonth) : "-");

        addValue("sessionAverage", sessionAverage > 0L ? FormatUtils.formatTimeAmount(sessionAverage) : "-");
        addValue("sessionAverageDay", sessionAverageDay > 0L ? FormatUtils.formatTimeAmount(sessionAverageDay) : "-");
        addValue("sessionAverageWeek", sessionAverageWeek > 0L ? FormatUtils.formatTimeAmount(sessionAverageWeek) : "-");
        addValue("sessionAverageMonth", sessionAverageMonth > 0L ? FormatUtils.formatTimeAmount(sessionAverageMonth) : "-");

        addValue("sessionCount", sessionCount);
        addValue("sessionCountDay", sessionCountDay);
        addValue("sessionCountWeek", sessionCountWeek);
        addValue("sessionCountMonth", sessionCountMonth);

        List<Action> actions = profile.getAllActions();
        addValue("tableBodyActions", new ActionsTable(actions).parseBody());

        String punchCardData = new PunchCardGraph(allSessions).toHighChartsSeries();
        WorldTimes worldTimes = profile.getWorldTimes();

        WorldPie worldPie = new WorldPie(worldTimes);

        addValue("worldPieSeries", worldPie.toHighChartsSeries());
        addValue("gmSeries", worldPie.toHighChartsDrilldown());

        addValue("punchCardSeries", punchCardData);

        List<Session> sessionsInLengthOrder = allSessions.stream()
                .sorted(new SessionLengthComparator())
                .collect(Collectors.toList());
        if (sessionsInLengthOrder.isEmpty()) {
            addValue("sessionLengthMedian", "-");
            addValue("sessionLengthLongest", "-");
        } else {
            Session medianSession = sessionsInLengthOrder.get(sessionsInLengthOrder.size() / 2);
            addValue("sessionLengthMedian", FormatUtils.formatTimeAmount(medianSession.getLength()));
            addValue("sessionLengthLongest", FormatUtils.formatTimeAmount(sessionsInLengthOrder.get(0).getLength()));
        }

        long playerKillCount = allSessions.stream().map(Session::getPlayerKills).mapToLong(Collection::size).sum();
        long mobKillCount = allSessions.stream().mapToLong(Session::getMobKills).sum();
        long deathCount = allSessions.stream().mapToLong(Session::getDeaths).sum();

        addValue("playerKillCount", playerKillCount);
        addValue("mobKillCount", mobKillCount);
        addValue("deathCount", deathCount);

        ActivityIndex activityIndex = profile.getActivityIndex(now);

        addValue("activityIndexNumber", activityIndex.getFormattedValue());
        addValue("activityIndexColor", activityIndex.getColor());
        addValue("activityIndex", activityIndex.getGroup());

        addValue("playerStatus", HtmlStructure.playerStatus(online, profile.getBannedOnServers(), profile.isOp()));

        if (!InfoSystem.getInstance().getConnectionSystem().isServerAvailable()) {
            addValue("networkName", Settings.SERVER_NAME.toString().replaceAll("[^a-zA-Z0-9_\\s]", "_"));
        }

        return HtmlUtils.replacePlaceholders(FileUtil.getStringFromResource("web/player.html"), placeHolders);
    }
}
