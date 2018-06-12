/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.Actions;
import com.djrapitops.plan.data.container.Action;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.CacheSystem;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.Map;
import java.util.UUID;

/**
 * Thread that is run when JVM shuts down.
 * <p>
 * Saves active sessions to the Database (PlayerQuitEvent is not called)
 *
 * @author Rsl1122
 */
public class ShutdownHook extends Thread {

    private static boolean activated = false;

    private static boolean isActivated() {
        return activated;
    }

    private static void activate(ShutdownHook hook) {
        activated = true;
        Runtime.getRuntime().addShutdownHook(hook);
    }

    public void register() {
        if (isActivated()) {
            return;
        }
        activate(this);
    }

    @Override
    public void run() {
        Log.debug("Shutdown hook triggered.");

        Database db = null;
        try {
            Map<UUID, Session> activeSessions = SessionCache.getActiveSessions();
            long now = System.currentTimeMillis();
            db = Database.getActive();
            saveFirstSessionInformation(db, now);
            saveActiveSessions(db, activeSessions, now);
        } catch (IllegalStateException ignored) {
            /* Database is not initialized */
        } catch (DBInitException e) {
            Log.toLog(this.getClass(), e);
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (DBException e) {
                    Log.toLog(this.getClass(), e);
                }
            }
        }
    }

    private void saveFirstSessionInformation(Database db, long now) throws DBInitException {
        DataCache dataCache = CacheSystem.getInstance().getDataCache();
        for (Map.Entry<UUID, Integer> entry : dataCache.getFirstSessionMsgCounts().entrySet()) {
            if (!db.isOpen()) {
                db.init();
            }
            try {
                UUID uuid = entry.getKey();
                int messagesSent = entry.getValue();
                db.save().action(uuid, new Action(now, Actions.FIRST_LOGOUT, "Messages sent: " + messagesSent));
            } catch (DBException e) {
                Log.toLog(this.getClass(), e);
            }
        }
    }

    private void saveActiveSessions(Database db, Map<UUID, Session> activeSessions, long now) throws DBInitException {
        for (Map.Entry<UUID, Session> entry : activeSessions.entrySet()) {
            UUID uuid = entry.getKey();
            Session session = entry.getValue();
            long sessionEnd = session.getSessionEnd();
            if (sessionEnd == -1) {
                session.endSession(now);
            }
            if (!db.isOpen()) {
                db.init();
            }
            try {
                Log.debug("Shutdown: Saving a session: " + session.getSessionStart());
                db.save().session(uuid, session);
            } catch (DBException e) {
                Log.toLog(this.getClass(), e);
            }
        }
        activeSessions.clear();
    }
}
