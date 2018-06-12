/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.data;

import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import com.djrapitops.plan.utilities.analysis.MathUtils;
import com.djrapitops.plan.utilities.comparators.PlayerProfileLastPlayedComparator;
import com.djrapitops.plan.utilities.comparators.TPSComparator;
import com.djrapitops.plan.utilities.html.tables.PlayersTableCreator;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Data class for streamlining Analysis data.
 * <p>
 * Most of the methods are not the most efficient when multiple of them are used.
 *
 * @author Rsl1122
 */
public class ServerProfile {

    private final UUID serverUUID;

    // Database information
    private List<PlayerProfile> players;
    private List<TPS> tps;
    private Map<String, Integer> commandUsage;

    // Information calculated with SQL
    private WorldTimes serverWorldtimes;
    private long lastPeakDate;
    private int lastPeakPlayers;
    private long allTimePeak;
    private int allTimePeakPlayers;

    // Calculated once
    private Map<UUID, PlayerProfile> playerMap;

    public ServerProfile(UUID serverUUID) {
        this.serverUUID = serverUUID;
        players = new ArrayList<>();
        tps = new ArrayList<>();
        commandUsage = new HashMap<>();

        allTimePeak = -1;
        allTimePeakPlayers = -1;
        lastPeakDate = -1;
        lastPeakPlayers = -1;
    }

    public static long getLowSpikeCount(List<TPS> tpsData) {
        int mediumThreshold = Settings.THEME_GRAPH_TPS_THRESHOLD_MED.getNumber();

        boolean wasLow = false;
        long spikeCount = 0L;

        for (TPS tpsObj : tpsData) {
            double tps = tpsObj.getTicksPerSecond();
            if (tps < mediumThreshold) {
                if (!wasLow) {
                    spikeCount++;
                    wasLow = true;
                }
            } else {
                wasLow = false;
            }
        }

        return spikeCount;
    }

    public static List<PlayerKill> getPlayerKills(List<Session> s) {
        List<PlayerKill> kills = new ArrayList<>();
        for (Session session : s) {
            kills.addAll(session.getPlayerKills());
        }
        return kills;
    }

    public static long getMobKillCount(List<Session> s) {
        long total = 0;
        for (Session session : s) {
            total += session.getMobKills();
        }
        return total;
    }

    public static long getDeathCount(List<Session> s) {
        long total = 0;
        for (Session session : s) {
            total += session.getDeaths();
        }
        return total;
    }

    public static long serverDownTime(List<TPS> tpsData) {
        long lastDate = -1;
        long downTime = 0;
        for (TPS tps : tpsData) {
            long date = tps.getDate();
            if (lastDate == -1) {
                lastDate = date;
                continue;
            }

            long diff = date - lastDate;
            if (diff > TimeAmount.MINUTE.ms() * 3L) {
                downTime += diff;
            }
            lastDate = date;
        }

        return downTime;
    }

    public static long serverIdleTime(List<TPS> tpsData) {
        long lastDate = -1;
        int lastPlayers = 0;
        long idleTime = 0;
        for (TPS tps : tpsData) {
            long date = tps.getDate();
            int players = tps.getPlayers();
            if (lastDate == -1) {
                lastDate = date;
                lastPlayers = players;
                continue;
            }

            long diff = date - lastDate;
            if (lastPlayers == 0 && players == 0) {
                idleTime += diff;
            }

            lastDate = date;
            lastPlayers = players;
        }

        return idleTime;
    }

    public static double aboveLowThreshold(List<TPS> tpsData) {
        if (tpsData.isEmpty()) {
            return 1;
        }

        int threshold = Settings.THEME_GRAPH_TPS_THRESHOLD_MED.getNumber();

        long count = 0;
        for (TPS tps : tpsData) {
            if (tps.getTicksPerSecond() >= threshold) {
                count++;
            }
        }

        return count * 1.0 / tpsData.size();
    }

    public List<PlayerProfile> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerProfile> players) {
        this.players = players;
    }

    public List<TPS> getTps() {
        return tps;
    }

    public void setTps(List<TPS> tps) {
        this.tps = tps;
    }

    public Map<String, Integer> getCommandUsage() {
        return commandUsage;
    }

    public void setCommandUsage(Map<String, Integer> commandUsage) {
        this.commandUsage = commandUsage;
    }

    public double getAverageTPS(long after, long before) {
        OptionalDouble average = getTPSData(after, before)
                .mapToDouble(TPS::getTicksPerSecond)
                .average();
        if (average.isPresent()) {
            return average.getAsDouble();
        }
        return -1;
    }

    public double getAverageCPU(long after, long before) {
        OptionalDouble average = getTPSData(after, before)
                .mapToDouble(TPS::getCPUUsage)
                .filter(num -> num >= 0)
                .average();
        if (average.isPresent()) {
            return average.getAsDouble();
        }
        return -1;
    }

    public double getAverageRAM(long after, long before) {
        OptionalDouble average = getTPSData(after, before)
                .mapToDouble(TPS::getUsedMemory)
                .average();
        if (average.isPresent()) {
            return average.getAsDouble();
        }
        return -1;
    }

    public double getAverageEntities(long after, long before) {
        OptionalDouble average = getTPSData(after, before)
                .mapToDouble(TPS::getEntityCount)
                .average();
        if (average.isPresent()) {
            return average.getAsDouble();
        }
        return -1;
    }

    public double getAverageChunks(long after, long before) {
        OptionalDouble average = getTPSData(after, before)
                .mapToDouble(TPS::getChunksLoaded)
                .average();
        if (average.isPresent()) {
            return average.getAsDouble();
        }
        return -1;
    }

    public long getNewPlayers(long after, long before) {
        return getPlayersWhoRegistered(after, before).count();
    }

    public long getUniquePlayers(long after, long before) {
        return getPlayersWhoPlayedBetween(after, before).count();
    }

    public double getNewPlayersPerDay(long after, long before) {
        long days = AnalysisUtils.getNumberOfDaysBetween(after, before);
        long newPlayers = getNewPlayers(after, before);
        return days == 0 ? newPlayers : newPlayers * 1.0 / days;
    }

    public Stream<PlayerProfile> getPlayersWhoPlayedBetween(long after, long before) {
        return players.stream()
                .filter(player -> player.playedBetween(after, before));
    }

    public Stream<PlayerProfile> getPlayersWhoRegistered(long after, long before) {
        return players.stream()
                .filter(player -> player.getRegistered() >= after && player.getRegistered() <= before);
    }

    public Stream<TPS> getTPSData(long after, long before) {
        return tps.stream().filter(tps -> tps.getDate() >= after && tps.getDate() <= before);
    }

    public String createPlayersTableBody() {
        players.sort(new PlayerProfileLastPlayedComparator());
        return PlayersTableCreator.createTable(players);
    }

    public List<String> getGeoLocations() {
        return players.stream()
                .map(PlayerProfile::getMostRecentGeoInfo)
                .map(GeoInfo::getGeolocation)
                .collect(Collectors.toList());
    }

    // Default setters & getters

    public long getTotalPlaytime() {
        return serverWorldtimes.getTotal();
    }

    public long getAveragePlayTime() {
        return MathUtils.averageLong(getTotalPlaytime(), getPlayerCount());
    }

    public long getPlayerCount() {
        return players.size();
    }

    public Map<UUID, List<Session>> getSessions() {
        return players.stream().collect(Collectors.toMap(PlayerProfile::getUuid, p -> p.getSessions(serverUUID)));
    }

    public List<Session> getAllSessions() {
        return players.stream().map(p -> p.getSessions(serverUUID)).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public WorldTimes getServerWorldtimes() {
        return serverWorldtimes;
    }

    public void setServerWorldtimes(WorldTimes serverWorldtimes) {
        this.serverWorldtimes = serverWorldtimes;
    }

    public long getLastPeakDate() {
        return lastPeakDate;
    }

    public void setLastPeakDate(long lastPeakDate) {
        this.lastPeakDate = lastPeakDate;
    }

    public int getLastPeakPlayers() {
        return lastPeakPlayers;
    }

    public void setLastPeakPlayers(int lastPeakPlayers) {
        this.lastPeakPlayers = lastPeakPlayers;
    }

    public long getAllTimePeak() {
        return allTimePeak;
    }

    public void setAllTimePeak(long allTimePeak) {
        this.allTimePeak = allTimePeak;
    }

    public int getAllTimePeakPlayers() {
        return allTimePeakPlayers;
    }

    public void setAllTimePeakPlayers(int allTimePeakPlayers) {
        this.allTimePeakPlayers = allTimePeakPlayers;
    }

    public Stream<PlayerProfile> getOps() {
        return players.stream().filter(PlayerProfile::isOp);
    }

    public Set<UUID> getUuids() {
        Set<UUID> uuids = new HashSet<>();
        for (PlayerProfile player : players) {
            uuids.add(player.getUuid());
        }
        return uuids;
    }

    public long serverDownTime(long after, long before) {
        return serverDownTime(getTPSData(after, before)
                .sorted(new TPSComparator())
                .collect(Collectors.toList()));
    }

    public long serverIdleTime(long after, long before) {
        return serverIdleTime(getTPSData(after, before)
                .sorted(new TPSComparator())
                .collect(Collectors.toList()));
    }

    public PlayerProfile getPlayer(UUID uuid) {
        if (playerMap == null) {
            playerMap = players.stream().collect(Collectors.toMap(PlayerProfile::getUuid, Function.identity()));
        }

        return playerMap.get(uuid);
    }

    public void addActiveSessions(Map<UUID, Session> activeSessions) {
        for (Map.Entry<UUID, Session> entry : activeSessions.entrySet()) {
            UUID uuid = entry.getKey();
            Session session = entry.getValue();
            session.setSessionID((int) session.getSessionStart());

            PlayerProfile player = getPlayer(uuid);
            if (player != null) {
                player.addActiveSession(session);
            }
        }
    }
}
