/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.processing.importing.importers;

import com.djrapitops.plugin.utilities.Verify;
import com.google.common.collect.ImmutableMap;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.UserInfo;
import main.java.com.djrapitops.plan.data.time.WorldTimes;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.systems.cache.GeolocationCache;
import main.java.com.djrapitops.plan.systems.processing.importing.ServerImportData;
import main.java.com.djrapitops.plan.systems.processing.importing.UserImportData;
import main.java.com.djrapitops.plan.systems.processing.importing.UserImportRefiner;
import main.java.com.djrapitops.plan.utilities.Benchmark;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Fuzzlemann
 * @since 4.0.0
 */
public abstract class Importer {

    public abstract List<String> getNames();

    public abstract ServerImportData getServerImportData();

    public abstract List<UserImportData> getUserImportData();

    public final void processImport() throws SQLException {
        String benchmarkName = "Import processing";
        String serverBenchmarkName = "Server Data processing";
        String userDataBenchmarkName = "User Data processing";

        Benchmark.start(benchmarkName);

        ExecutorService service = Executors.newCachedThreadPool();

        new ImportExecutorHelper() {
            @Override
            void execute() throws SQLException {
                Benchmark.start(serverBenchmarkName);
                processServerData();
                Benchmark.stop(serverBenchmarkName);
            }
        }.submit(service);

        new ImportExecutorHelper() {
            @Override
            void execute() throws SQLException {
                Benchmark.start(userDataBenchmarkName);
                processUserData();
                Benchmark.stop(userDataBenchmarkName);
            }
        }.submit(service);

        service.shutdown();

        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Log.toLog(this.getClass().getName(), e);
        }

        Benchmark.stop(benchmarkName);
    }

    private void processServerData() throws SQLException {
        String benchmarkName = "Processing Server Data";
        String getDataBenchmarkName = "Getting Server Data";
        String insertDataIntoDatabaseBenchmarkName = "Insert Server Data into Database";

        Benchmark.start(benchmarkName);
        Benchmark.start(getDataBenchmarkName);

        ServerImportData serverImportData = getServerImportData();

        Benchmark.stop(getDataBenchmarkName);

        if (serverImportData == null) {
            Log.debug("Server Import Data null, skipping");
            return;
        }

        Plan plan = Plan.getInstance();
        UUID uuid = plan.getServerInfoManager().getServerUUID();
        Database db = plan.getDB();

        ExecutorService service = Executors.newCachedThreadPool();

        Benchmark.start(insertDataIntoDatabaseBenchmarkName);

        new ImportExecutorHelper() {
            @Override
            void execute() throws SQLException {
                db.getTpsTable().insertAllTPS(ImmutableMap.of(uuid, serverImportData.getTpsData()));
            }
        }.submit(service);

        new ImportExecutorHelper() {
            @Override
            void execute() throws SQLException {
                db.getCommandUseTable().insertCommandUsage(ImmutableMap.of(uuid, serverImportData.getCommandUsages()));
            }
        }.submit(service);

        service.shutdown();

        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Log.toLog(this.getClass().getName(), e);
        }

        Benchmark.stop(insertDataIntoDatabaseBenchmarkName);
        Benchmark.stop(benchmarkName);
    }

    private void processUserData() throws SQLException {
        String benchmarkName = "Processing User Data";
        String getDataBenchmarkName = "Getting User Data";
        String insertDataIntoCollectionsBenchmarkName = "Insert User Data into Collections";
        String insertDataIntoDatabaseBenchmarkName = "Insert User Data into Database";

        Benchmark.start(benchmarkName);
        Benchmark.start(getDataBenchmarkName);

        List<UserImportData> userImportData = getUserImportData();
        Benchmark.stop(getDataBenchmarkName);

        if (Verify.isEmpty(userImportData)) {
            Log.debug("User Import Data null or empty, skipping");
            return;
        }

        Plan plan = Plan.getInstance();

        UserImportRefiner userImportRefiner = new UserImportRefiner(plan, userImportData);
        userImportData = userImportRefiner.refineData();

        UUID serverUUID = plan.getServerInfoManager().getServerUUID();
        Database db = plan.getDB();

        Set<UUID> existingUUIDs = db.getSavedUUIDs();

        Benchmark.start(insertDataIntoCollectionsBenchmarkName);

        Map<UUID, UserInfo> users = new Hashtable<>();
        List<UserInfo> userInfo = new Vector<>();
        Map<UUID, List<String>> nickNames = new Hashtable<>();
        Map<UUID, List<Session>> sessions = new Hashtable<>();
        Map<UUID, Map<String, String>> ips = new Hashtable<>();
        Map<UUID, Integer> timesKicked = new Hashtable<>();

        userImportData.parallelStream().forEach(data -> {
            UUID uuid = data.getUuid();
            UserInfo info = toUserInfo(data);

            if (!existingUUIDs.contains(uuid)) {
                userInfo.add(info);
            }

            users.put(uuid, info);
            nickNames.put(uuid, data.getNicknames());
            ips.put(uuid, convertIPs(data));
            timesKicked.put(uuid, data.getTimesKicked());
            sessions.put(uuid, Collections.singletonList(toSession(data)));
        });

        Benchmark.stop(insertDataIntoCollectionsBenchmarkName);

        ExecutorService service = Executors.newCachedThreadPool();

        Benchmark.start(insertDataIntoDatabaseBenchmarkName);

        db.getUsersTable().insertUsers(users);

        new ImportExecutorHelper() {
            @Override
            void execute() throws SQLException {
                db.getSessionsTable().insertSessions(ImmutableMap.of(serverUUID, sessions), true);
            }
        }.submit(service);

        new ImportExecutorHelper() {
            @Override
            void execute() throws SQLException {
                db.getUsersTable().updateKicked(timesKicked);
            }
        }.submit(service);

        new ImportExecutorHelper() {
            @Override
            void execute() throws SQLException {
                db.getUserInfoTable().insertUserInfo(ImmutableMap.of(serverUUID, userInfo));
            }
        }.submit(service);

        new ImportExecutorHelper() {
            @Override
            void execute() throws SQLException {
                db.getNicknamesTable().insertNicknames(ImmutableMap.of(serverUUID, nickNames));
            }
        }.submit(service);

        new ImportExecutorHelper() {
            @Override
            void execute() throws SQLException {
                db.getIpsTable().insertIPsAndGeolocations(ips);
            }
        }.submit(service);

        service.shutdown();

        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Log.toLog(this.getClass().getName(), e);
        }

        Benchmark.stop(insertDataIntoDatabaseBenchmarkName);
        Benchmark.stop(benchmarkName);
    }

    private UserInfo toUserInfo(UserImportData userImportData) {
        UUID uuid = userImportData.getUuid();
        String name = userImportData.getName();
        long registered = userImportData.getRegistered();
        boolean op = userImportData.isOp();
        boolean banned = userImportData.isBanned();

        return new UserInfo(uuid, name, registered, op, banned);
    }

    private Session toSession(UserImportData userImportData) {
        int mobKills = userImportData.getMobKills();
        int deaths = userImportData.getDeaths();

        Session session = new Session(0, 0L, 0L, mobKills, deaths);

        session.setPlayerKills(userImportData.getKills());
        session.setWorldTimes(new WorldTimes(userImportData.getWorldTimes()));

        return session;
    }

    private Map<String, String> convertIPs(UserImportData userImportData) {
        Map<String, String> convertedIPs;
        List<String> ips = userImportData.getIps();

        convertedIPs = ips.parallelStream()
                .collect(Collectors.toMap(ip -> ip, GeolocationCache::getCountry, (a, b) -> b, HashMap::new));

        return convertedIPs;
    }

    private abstract class ImportExecutorHelper {
        abstract void execute() throws SQLException;

        void submit(ExecutorService service) {
            service.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        execute();
                    } catch (SQLException e) {
                        Log.toLog(this.getClass().getName(), e);
                    }
                }
            });
        }
    }
}
