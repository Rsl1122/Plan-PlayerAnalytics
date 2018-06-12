/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database.databases.operation;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.data.container.*;
import com.djrapitops.plan.system.info.server.Server;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Operation methods for saving data.
 *
 * Note: Method names subject to change (TODO remove insert update and such)
 *
 * @author Rsl1122
 */
public interface SaveOperations {

    // Bulk save

    void insertTPS(Map<UUID, List<TPS>> ofServers) throws DBException;

    void insertCommandUsage(Map<UUID, Map<String, Integer>> ofServers) throws DBException;

    void insertUsers(Map<UUID, UserInfo> ofServers) throws DBException;

    void insertSessions(Map<UUID, Map<UUID, List<Session>>> ofServers, boolean containsExtraData)
            throws DBException;

    void kickAmount(Map<UUID, Integer> ofUsers) throws DBException;

    void insertUserInfo(Map<UUID, List<UserInfo>> ofServers) throws DBException;

    void insertNicknames(Map<UUID, Map<UUID, List<String>>> ofServers) throws DBException;

    void insertAllGeoInfo(Map<UUID, List<GeoInfo>> ofUsers) throws DBException;

    // Single data point

    void banStatus(UUID uuid, boolean banned) throws DBException;

    void opStatus(UUID uuid, boolean op) throws DBException;

    void registerNewUser(UUID uuid, long registered, String name) throws DBException;

    void action(UUID uuid, Action action) throws DBException;

    void geoInfo(UUID uuid, GeoInfo geoInfo) throws DBException;

    void playerWasKicked(UUID uuid) throws DBException;

    void playerName(UUID uuid, String playerName) throws DBException;

    void playerDisplayName(UUID uuid, String displayName) throws DBException;

    void registerNewUserOnThisServer(UUID uuid, long registered) throws DBException;

    void commandUsed(String commandName) throws DBException;

    void insertTPSforThisServer(TPS tps) throws DBException;

    void session(UUID uuid, Session session) throws DBException;

    void serverInfoForThisServer(Server server) throws DBException;

    void webUser(WebUser webUser) throws DBException;
}
