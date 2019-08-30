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
package com.djrapitops.plan.system.json;

import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.mutators.*;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.system.settings.config.paths.TimeSettings;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.system.storage.database.DBSystem;
import com.djrapitops.plan.system.storage.database.Database;
import com.djrapitops.plan.system.storage.database.access.queries.containers.PlayerContainerQuery;
import com.djrapitops.plan.system.storage.database.access.queries.objects.ServerQueries;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.graphs.Graphs;
import com.djrapitops.plan.utilities.html.graphs.pie.WorldPie;
import com.djrapitops.plan.utilities.html.structure.ServerAccordion;
import org.apache.commons.text.StringEscapeUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public class PlayerJSONParser {

    private final PlanConfig config;
    private final Theme theme;
    private final DBSystem dbSystem;
    private final Graphs graphs;
    private final Formatters formatters;

    private final Formatter<Long> timeAmount;
    private final Formatter<Double> decimals;
    private final Formatter<Long> year;

    @Inject
    public PlayerJSONParser(
            PlanConfig config,
            Theme theme,
            DBSystem dbSystem,
            Formatters formatters,
            Graphs graphs
    ) {
        this.config = config;
        this.theme = theme;
        this.dbSystem = dbSystem;

        this.formatters = formatters;
        timeAmount = formatters.timeAmount();
        decimals = formatters.decimals();
        year = formatters.yearLong();
        this.graphs = graphs;
    }

    public Map<String, Object> createJSONAsMap(UUID playerUUID) {
        Database db = dbSystem.getDatabase();

        Map<UUID, String> serverNames = db.query(ServerQueries.fetchServerNames());
        String[] pieColors = Arrays.stream(theme.getValue(ThemeVal.GRAPH_WORLD_PIE).split(","))
                .map(color -> color.trim().replace("\"", ""))
                .toArray(String[]::new);

        PlayerContainer player = db.query(new PlayerContainerQuery(playerUUID));
        SessionsMutator sessionsMutator = SessionsMutator.forContainer(player);
        Map<UUID, WorldTimes> worldTimesPerServer = PerServerMutator.forContainer(player).worldTimesPerServer();
        List<Map<String, Object>> serverAccordion = new ServerAccordion(player, serverNames, graphs, year, timeAmount).asMaps();
        List<PlayerKill> kills = player.getValue(PlayerKeys.PLAYER_KILLS).orElse(Collections.emptyList());
        List<PlayerKill> deaths = player.getValue(PlayerKeys.PLAYER_DEATHS_KILLS).orElse(Collections.emptyList());

        Map<String, Object> data = new HashMap<>();
        data.put("info", createInfoJSONMap(player, serverNames));
        data.put("online_activity", createOnlineActivityJSONMap(sessionsMutator));
        data.put("kill_data", createPvPPvEMap(player));

        data.put("nicknames", player.getValue(PlayerKeys.NICKNAMES)
                .map(nicks -> Nickname.fromDataNicknames(nicks, serverNames, year))
                .orElse(Collections.emptyList()));
        data.put("connections", player.getValue(PlayerKeys.GEO_INFO)
                .map(geoInfo -> ConnectionInfo.fromGeoInfo(geoInfo, year))
                .orElse(Collections.emptyList()));
        data.put("player_kills", new PlayerKillMutator(kills).filterNonSelfKills().toJSONAsMap(formatters));
        data.put("player_deaths", new PlayerKillMutator(deaths).toJSONAsMap(formatters));
        data.put("sessions", sessionsMutator.toServerNameJSONMaps(graphs, config.getWorldAliasSettings(), formatters));
        data.put("sessions_per_page", config.get(DisplaySettings.SESSIONS_PER_PAGE));
        data.put("servers", serverAccordion);
        data.put("punchcard_series", graphs.special().punchCard(sessionsMutator).getDots());
        WorldPie worldPie = graphs.pie().worldPie(player.getValue(PlayerKeys.WORLD_TIMES).orElse(new WorldTimes()));
        data.put("world_pie_series", worldPie.getSlices());
        data.put("gm_series", worldPie.toHighChartsDrillDownMaps());
        data.put("calendar_series", graphs.calendar().playerCalendar(player).getEntries());
        data.put("server_pie_series", graphs.pie().serverPreferencePie(serverNames, worldTimesPerServer).getSlices());
        data.put("server_pie_colors", pieColors);
        data.put("first_day", 1); // Monday
        return data;
    }

    private Map<String, Object> createOnlineActivityJSONMap(SessionsMutator sessionsMutator) {
        long now = System.currentTimeMillis();
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);
        long weekAgo = now - TimeUnit.DAYS.toMillis(7L);
        SessionsMutator sessions30d = sessionsMutator.filterSessionsBetween(monthAgo, now);
        SessionsMutator sessions7d = sessions30d.filterSessionsBetween(weekAgo, now);

        Map<String, Object> onlineActivity = new HashMap<>();

        onlineActivity.put("playtime_30d", timeAmount.apply(sessions30d.toPlaytime()));
        onlineActivity.put("active_playtime_30d", timeAmount.apply(sessions30d.toActivePlaytime()));
        onlineActivity.put("afk_time_30d", timeAmount.apply(sessions30d.toAfkTime()));
        onlineActivity.put("average_session_length_30d", timeAmount.apply(sessions30d.toAverageSessionLength()));
        onlineActivity.put("session_count_30d", sessions30d.count());
        onlineActivity.put("player_kill_count_30d", sessions30d.toPlayerKillCount());
        onlineActivity.put("mob_kill_count_30d", sessions30d.toMobKillCount());
        onlineActivity.put("death_count_30d", sessions30d.toDeathCount());

        onlineActivity.put("playtime_7d", timeAmount.apply(sessions7d.toPlaytime()));
        onlineActivity.put("active_playtime_7d", timeAmount.apply(sessions7d.toActivePlaytime()));
        onlineActivity.put("afk_time_7d", timeAmount.apply(sessions7d.toAfkTime()));
        onlineActivity.put("average_session_length_7d", timeAmount.apply(sessions7d.toAverageSessionLength()));
        onlineActivity.put("session_count_7d", sessions7d.count());
        onlineActivity.put("player_kill_count_7d", sessions7d.toPlayerKillCount());
        onlineActivity.put("mob_kill_count_7d", sessions7d.toMobKillCount());
        onlineActivity.put("death_count_7d", sessions7d.toDeathCount());

        return onlineActivity;
    }

    private Map<String, Object> createInfoJSONMap(PlayerContainer player, Map<UUID, String> serverNames) {
        SessionsMutator sessions = SessionsMutator.forContainer(player);
        ActivityIndex activityIndex = player.getActivityIndex(System.currentTimeMillis(), config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD));
        PerServerMutator perServer = PerServerMutator.forContainer(player);
        PingMutator ping = PingMutator.forContainer(player);

        Map<String, Object> info = new HashMap<>();

        info.put("online", SessionCache.getCachedSession(player.getUnsafe(PlayerKeys.UUID)).isPresent());
        info.put("operator", player.getValue(PlayerKeys.OPERATOR).orElse(false));
        info.put("banned", player.getValue(PlayerKeys.BANNED).orElse(false));
        info.put("kick_count", player.getValue(PlayerKeys.KICK_COUNT).orElse(0));
        info.put("player_kill_count", player.getValue(PlayerKeys.PLAYER_KILL_COUNT).orElse(0));
        info.put("mob_kill_count", player.getValue(PlayerKeys.MOB_KILL_COUNT).orElse(0));
        info.put("death_count", player.getValue(PlayerKeys.DEATH_COUNT).orElse(0));
        info.put("playtime", timeAmount.apply(sessions.toPlaytime()));
        info.put("active_playtime", timeAmount.apply(sessions.toActivePlaytime()));
        info.put("afk_time", timeAmount.apply(sessions.toAfkTime()));
        info.put("session_count", sessions.count());
        info.put("longest_session_length", timeAmount.apply(sessions.toLongestSessionLength()));
        info.put("session_median", timeAmount.apply(sessions.toMedianSessionLength()));
        info.put("activity_index", decimals.apply(activityIndex.getValue()));
        info.put("activity_index_group", activityIndex.getGroup());
        UUID favoriteServer = perServer.favoriteServer();
        info.put("favorite_server", serverNames.getOrDefault(favoriteServer, favoriteServer.toString()));
        double averagePing = ping.average();
        int worstPing = ping.max();
        int bestPing = ping.min();

        String unavailable = "Unavailable";
        info.put("average_ping", averagePing != -1.0 ? decimals.apply(averagePing) + " ms" : unavailable);
        info.put("worst_ping", worstPing != -1.0 ? worstPing + " ms" : unavailable);
        info.put("best_ping", bestPing != -1.0 ? bestPing + " ms" : unavailable);
        info.put("registered", player.getValue(PlayerKeys.REGISTERED).map(year).orElse("-"));
        info.put("last_seen", player.getValue(PlayerKeys.LAST_SEEN).map(year).orElse("-"));

        return info;
    }

    private Map<String, Object> createPvPPvEMap(PlayerContainer playerContainer) {
        long now = System.currentTimeMillis();
        long weekAgo = now - TimeUnit.DAYS.toMillis(7L);
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);

        PlayerVersusMutator playerVersus = PlayerVersusMutator.forContainer(playerContainer);
        PlayerVersusMutator playerVersus30d = playerVersus.filterBetween(monthAgo, now);
        PlayerVersusMutator playerVersus7d = playerVersus30d.filterBetween(weekAgo, now);

        Map<String, Object> killData = new HashMap<>();
        int pks = playerVersus.toPlayerKillCount();
        int pks7d = playerVersus7d.toPlayerKillCount();
        int pks30d = playerVersus30d.toPlayerKillCount();
        killData.put("player_kills_total", pks);
        killData.put("player_kills_30d", pks30d);
        killData.put("player_kills_7d", pks7d);

        int playerDeaths = playerVersus.toPlayerDeathCount();
        int playerDeaths30d = playerVersus30d.toPlayerDeathCount();
        int playerDeaths7d = playerVersus7d.toPlayerDeathCount();
        killData.put("player_deaths_total", playerDeaths);
        killData.put("player_deaths_30d", playerDeaths30d);
        killData.put("player_deaths_7d", playerDeaths7d);

        double kdr = playerDeaths != 0 ? (double) pks / playerDeaths : pks;
        double kdr30d = playerDeaths30d != 0 ? (double) pks30d / playerDeaths30d : pks30d;
        double krd7d = playerDeaths7d != 0 ? (double) pks7d / playerDeaths7d : pks7d;
        killData.put("player_kdr_total", decimals.apply(kdr));
        killData.put("player_kdr_30d", decimals.apply(kdr30d));
        killData.put("player_kdr_7d", decimals.apply(krd7d));

        int mobKills = playerVersus.toMobKillCount();
        int mobKills30d = playerVersus30d.toMobKillCount();
        int mobKills7d = playerVersus7d.toMobKillCount();
        killData.put("mob_kills_total", mobKills);
        killData.put("mob_kills_30d", mobKills30d);
        killData.put("mob_kills_7d", mobKills7d);

        int deaths = playerVersus.toDeathCount();
        int deaths30d = playerVersus30d.toDeathCount();
        int deaths7d = playerVersus7d.toDeathCount();
        killData.put("deaths_total", deaths);
        killData.put("deaths_30d", deaths30d);
        killData.put("deaths_7d", deaths7d);

        int mobDeaths = deaths - playerDeaths;
        int mobDeaths30d = deaths30d - playerDeaths30d;
        int mobDeaths7d = deaths7d - playerDeaths7d;

        killData.put("mob_deaths_total", mobDeaths);
        killData.put("mob_deaths_30d", mobDeaths30d);
        killData.put("mob_deaths_7d", mobDeaths7d);

        double mobKdr = mobDeaths != 0 ? (double) mobKills / mobDeaths : mobKills;
        double mobKdr30d = mobDeaths30d != 0 ? (double) mobKills30d / mobDeaths30d : mobKills30d;
        double mobKdr7d = mobDeaths7d != 0 ? (double) mobKills7d / mobDeaths7d : mobKills7d;
        killData.put("mob_kdr_total", decimals.apply(mobKdr));
        killData.put("mob_kdr_30d", decimals.apply(mobKdr30d));
        killData.put("mob_kdr_7d", decimals.apply(mobKdr7d));

        List<String> topWeapons = playerVersus.toTopWeapons(3);
        killData.put("weapon_1st", getWeapon(topWeapons, 0).orElse("-"));
        killData.put("weapon_2nd", getWeapon(topWeapons, 1).orElse("-"));
        killData.put("weapon_3rd", getWeapon(topWeapons, 2).orElse("-"));

        return killData;
    }

    private <T> Optional<T> getWeapon(List<T> list, int index) {
        return list.size() <= index ? Optional.empty() : Optional.of(list.get(index));
    }

    public static class Nickname {
        private String nickname;
        private String server;
        private String date;

        public Nickname(String nickname, String server, String date) {
            this.nickname = nickname;
            this.server = server;
            this.date = date;
        }

        public static List<Nickname> fromDataNicknames(
                List<com.djrapitops.plan.data.store.objects.Nickname> nicknames,
                Map<UUID, String> serverNames,
                Formatter<Long> dateFormatter
        ) {
            List<Nickname> mapped = new ArrayList<>();
            for (com.djrapitops.plan.data.store.objects.Nickname nickname : nicknames) {
                mapped.add(new Nickname(
                        Html.swapColorCodesToSpan(StringEscapeUtils.escapeHtml4(nickname.getName())),
                        serverNames.getOrDefault(nickname.getServerUUID(), nickname.getServerUUID().toString()),
                        dateFormatter.apply(nickname.getDate())
                ));
            }
            return mapped;
        }
    }

    public static class ConnectionInfo {
        private String geolocation;
        private String date;

        public ConnectionInfo(String geolocation, String date) {
            this.geolocation = geolocation;
            this.date = date;
        }

        public static List<ConnectionInfo> fromGeoInfo(List<GeoInfo> geoInfo, Formatter<Long> dateFormatter) {
            return geoInfo.stream()
                    .map(i -> new ConnectionInfo(i.getGeolocation(), dateFormatter.apply(i.getDate())))
                    .collect(Collectors.toList());
        }
    }

}
