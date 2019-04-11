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
package com.djrapitops.plan.db;

import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.data.container.*;
import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.containers.AnalysisContainer;
import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.containers.ServerContainer;
import com.djrapitops.plan.data.store.keys.*;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.store.objects.DateObj;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.data.time.GMTimes;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.db.access.Executable;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.queries.*;
import com.djrapitops.plan.db.access.queries.containers.AllPlayerContainersQuery;
import com.djrapitops.plan.db.access.queries.containers.ContainerFetchQueries;
import com.djrapitops.plan.db.access.queries.containers.ServerPlayerContainersQuery;
import com.djrapitops.plan.db.access.queries.objects.*;
import com.djrapitops.plan.db.access.transactions.BackupCopyTransaction;
import com.djrapitops.plan.db.access.transactions.StoreConfigTransaction;
import com.djrapitops.plan.db.access.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.db.access.transactions.commands.*;
import com.djrapitops.plan.db.access.transactions.events.*;
import com.djrapitops.plan.db.access.transactions.init.CreateIndexTransaction;
import com.djrapitops.plan.db.access.transactions.init.CreateTablesTransaction;
import com.djrapitops.plan.db.access.transactions.init.RemoveDuplicateUserInfoTransaction;
import com.djrapitops.plan.db.patches.Patch;
import com.djrapitops.plan.db.tasks.DBCleanTask;
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.ExtensionService;
import com.djrapitops.plan.extension.ExtensionServiceImplementation;
import com.djrapitops.plan.extension.annotation.*;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.results.ExtensionBooleanData;
import com.djrapitops.plan.extension.implementation.results.ExtensionStringData;
import com.djrapitops.plan.extension.implementation.results.ExtensionTabData;
import com.djrapitops.plan.extension.implementation.results.player.ExtensionPlayerData;
import com.djrapitops.plan.extension.implementation.results.server.ExtensionServerData;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionPlayerDataQuery;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionServerDataQuery;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.RemoveUnsatisfiedConditionalResultsTransaction;
import com.djrapitops.plan.extension.table.Table;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.config.Config;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DatabaseSettings;
import com.djrapitops.plan.system.settings.paths.WebserverSettings;
import com.djrapitops.plan.utilities.SHA256Hash;
import com.djrapitops.plan.utilities.comparators.DateHolderRecentComparator;
import com.djrapitops.plugin.logging.console.TestPluginLogger;
import com.djrapitops.plugin.logging.error.ConsoleErrorLogger;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;
import rules.ComponentMocker;
import rules.PluginComponentMocker;
import utilities.*;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Contains all common Database Tests for all Database Types
 *
 * @author Rsl1122 (Refactored into this class by Fuzzlemann)
 */
public abstract class CommonDBTest {

    private static final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    @ClassRule
    public static ComponentMocker component = new PluginComponentMocker(temporaryFolder);

    public static UUID serverUUID;

    public static DBSystem dbSystem;
    public static SQLDB db;
    public static PlanSystem system;

    public final String[] worlds = new String[]{"TestWorld", "TestWorld2"};
    public final UUID playerUUID = TestConstants.PLAYER_ONE_UUID;
    public final UUID player2UUID = TestConstants.PLAYER_TWO_UUID;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    static void handleSetup(String dbName) throws Exception {
        system = component.getPlanSystem();
        PlanConfig config = system.getConfigSystem().getConfig();
        config.set(WebserverSettings.PORT, TEST_PORT_NUMBER);
        config.set(DatabaseSettings.TYPE, dbName);
        system.enable();

        dbSystem = system.getDatabaseSystem();
        db = (SQLDB) dbSystem.getActiveDatabaseByName(dbName);
        db.setTransactionExecutorServiceProvider(MoreExecutors::newDirectExecutorService);
        db.init();

        serverUUID = system.getServerInfo().getServerUUID();
    }

    @AfterClass
    public static void tearDownClass() {
        if (system != null) system.disable();
    }

    @Before
    public void setUp() {
        db.executeTransaction(new Patch() {
            @Override
            public boolean hasBeenApplied() {
                return false;
            }

            @Override
            public void applyPatch() {
                dropTable("plan_world_times");
                dropTable("plan_kills");
                dropTable("plan_sessions");
                dropTable("plan_worlds");
                dropTable("plan_users");
            }
        });
        db.executeTransaction(new CreateTablesTransaction());
        db.executeTransaction(new RemoveEverythingTransaction());

        db.executeTransaction(new StoreServerInformationTransaction(new Server(-1, serverUUID, "ServerName", "", 20)));
        assertEquals(serverUUID, db.getServerUUIDSupplier().get());

        ExtensionService extensionService = system.getExtensionService();
        extensionService.unregister(new PlayerExtension());
        extensionService.unregister(new ServerExtension());
        extensionService.unregister(new ConditionalExtension());
        extensionService.unregister(new TableExtension());
    }

    private void execute(Executable executable) {
        db.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(executable);
            }
        });
    }

    public void commitTest() {
        db.close();
        db.init();
    }

    @Test
    public void testSaveCommandUse() {
        Map<String, Integer> expected = new HashMap<>();

        expected.put("plan", 1);
        expected.put("tp", 4);
        expected.put("pla", 7);
        expected.put("help", 21);

        useCommand("plan");
        useCommand("tp", 4);
        useCommand("pla", 7);
        useCommand("help", 21);
        useCommand("roiergbnougbierubieugbeigubeigubgierbgeugeg", 3);

        commitTest();

        Map<String, Integer> commandUse = db.query(ServerAggregateQueries.commandUsageCounts(serverUUID));
        assertEquals(expected, commandUse);
    }

    @Test
    public void commandUsageSavingDoesNotCreateNewEntriesForOldCommands() {
        Map<String, Integer> expected = new HashMap<>();

        expected.put("plan", 1);
        expected.put("test", 3);
        expected.put("tp", 6);
        expected.put("pla", 7);
        expected.put("help", 21);

        testSaveCommandUse();

        useCommand("test", 3);
        useCommand("tp", 2);

        Map<String, Integer> commandUse = db.query(ServerAggregateQueries.commandUsageCounts(serverUUID));
        assertEquals(expected, commandUse);
    }

    private void useCommand(String commandName) {
        db.executeTransaction(new CommandStoreTransaction(serverUUID, commandName));
    }

    private void useCommand(String commandName, int times) {
        for (int i = 0; i < times; i++) {
            useCommand(commandName);
        }
    }

    @Test
    public void testTPSSaving() throws Exception {
        Random r = new Random();

        List<TPS> expected = new ArrayList<>();

        for (int i = 0; i < RandomData.randomInt(1, 5); i++) {
            expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), r.nextDouble(), r.nextLong(), r.nextInt(), r.nextInt(), r.nextLong()));
        }

        for (TPS tps : expected) {
            execute(DataStoreQueries.storeTPS(serverUUID, tps));
        }

        commitTest();

        assertEquals(expected, db.query(TPSQueries.fetchTPSDataOfServer(serverUUID)));
    }

    private void saveUserOne() {
        playerIsRegisteredToBothTables();
        db.executeTransaction(new KickStoreTransaction(playerUUID));
    }

    private void saveUserTwo() {
        db.executeTransaction(new PlayerRegisterTransaction(player2UUID, () -> 123456789L, "Test"));
    }

    @Test
    public void geoInformationIsStored() throws NoSuchAlgorithmException {
        saveUserOne();

        String expectedIP = "1.2.3.4";
        String expectedGeoLoc = "TestLocation";
        long time = System.currentTimeMillis();

        saveGeoInfo(playerUUID, new GeoInfo(expectedIP, expectedGeoLoc, time, "3"));
        commitTest();

        List<GeoInfo> geolocations = db.query(GeoInfoQueries.fetchAllGeoInformation()).getOrDefault(playerUUID, new ArrayList<>());
        assertEquals(1, geolocations.size());

        GeoInfo expected = new GeoInfo("1.2.xx.xx", expectedGeoLoc, time, new SHA256Hash(expectedIP).create());
        assertEquals(expected, geolocations.get(0));
    }

    @Test
    public void testNicknamesTable() {
        saveUserOne();

        Nickname expected = new Nickname("TestNickname", System.currentTimeMillis(), serverUUID);
        db.executeTransaction(new NicknameStoreTransaction(playerUUID, expected, (uuid, name) -> false /* Not cached */));
        db.executeTransaction(new NicknameStoreTransaction(playerUUID, expected, (uuid, name) -> true /* Cached */));
        commitTest();

        List<Nickname> nicknames = db.query(NicknameQueries.fetchNicknameDataOfPlayer(playerUUID));
        assertEquals(1, nicknames.size());
        assertEquals(expected, nicknames.get(0));
    }

    @Test
    public void webUserIsRegistered() {
        WebUser expected = new WebUser(TestConstants.PLAYER_ONE_NAME, "RandomGarbageBlah", 0);
        db.executeTransaction(new RegisterWebUserTransaction(expected));
        commitTest();

        Optional<WebUser> found = db.query(WebUserQueries.fetchWebUser(TestConstants.PLAYER_ONE_NAME));
        assertTrue(found.isPresent());
        assertEquals(expected, found.get());
    }

    @Test
    public void multipleWebUsersAreFetchedAppropriately() {
        webUserIsRegistered();
        assertEquals(1, db.query(WebUserQueries.fetchAllPlanWebUsers()).size());
    }

    @Test
    public void webUserIsRemoved() {
        webUserIsRegistered();
        db.executeTransaction(new RemoveWebUserTransaction(TestConstants.PLAYER_ONE_NAME));
        assertFalse(db.query(WebUserQueries.fetchWebUser(TestConstants.PLAYER_ONE_NAME)).isPresent());
    }

    @Test
    public void worldNamesAreStored() {
        String[] expected = {"Test", "Test2", "Test3"};
        saveWorlds(expected);

        commitTest();

        Collection<String> result = db.query(LargeFetchQueries.fetchAllWorldNames()).getOrDefault(serverUUID, new HashSet<>());
        assertEquals(new HashSet<>(Arrays.asList(expected)), result);
    }

    private void saveWorld(String worldName) {
        db.executeTransaction(new WorldNameStoreTransaction(serverUUID, worldName));
    }

    private void saveWorlds(String... worldNames) {
        for (String worldName : worldNames) {
            saveWorld(worldName);
        }
    }

    private void saveTwoWorlds() {
        saveWorlds(worlds);
    }

    private WorldTimes createWorldTimes() {
        Map<String, GMTimes> times = new HashMap<>();
        Map<String, Long> gm = new HashMap<>();
        String[] gms = GMTimes.getGMKeyArray();
        gm.put(gms[0], 1000L);
        gm.put(gms[1], 2000L);
        gm.put(gms[2], 3000L);
        gm.put(gms[3], 4000L);

        String worldName = worlds[0];
        times.put(worldName, new GMTimes(gm));
        db.executeTransaction(new WorldNameStoreTransaction(serverUUID, worldName));

        return new WorldTimes(times);
    }

    private List<PlayerKill> createKills() {
        List<PlayerKill> kills = new ArrayList<>();
        kills.add(new PlayerKill(TestConstants.PLAYER_TWO_UUID, "Iron Sword", 4321L));
        kills.add(new PlayerKill(TestConstants.PLAYER_TWO_UUID, "Gold Sword", 5321L));
        kills.sort(new DateHolderRecentComparator());
        return kills;
    }

    @Test
    public void testSessionPlaytimeSaving() {
        saveTwoWorlds();
        saveUserOne();
        saveUserTwo();
        Session session = new Session(playerUUID, serverUUID, 12345L, worlds[0], "SURVIVAL");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        long expectedLength = 10000L;
        assertEquals(expectedLength, session.getLength());
        assertEquals(expectedLength, session.getUnsafe(SessionKeys.WORLD_TIMES).getTotal());

        execute(DataStoreQueries.storeSession(session));

        commitTest();

        Map<UUID, List<Session>> sessions = db.query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        assertTrue(sessions.containsKey(serverUUID));

        SessionsMutator sessionsMutator = new SessionsMutator(sessions.get(serverUUID));
        SessionsMutator afterTimeSessionsMutator = sessionsMutator.filterSessionsBetween(30000, System.currentTimeMillis());

        assertEquals(expectedLength, sessionsMutator.toPlaytime());
        assertEquals(0L, afterTimeSessionsMutator.toPlaytime());

        assertEquals(1, sessionsMutator.count());
        assertEquals(0, afterTimeSessionsMutator.count());
    }

    @Test
    public void testSessionSaving() {
        saveUserOne();
        saveUserTwo();

        Session session = new Session(playerUUID, serverUUID, 12345L, worlds[0], "SURVIVAL");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        execute(DataStoreQueries.storeSession(session));

        commitTest();

        Map<UUID, List<Session>> sessions = db.query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        List<Session> savedSessions = sessions.get(serverUUID);

        assertNotNull(savedSessions);
        assertEquals(1, savedSessions.size());
        assertNull(sessions.get(UUID.randomUUID()));

        assertEquals(session, savedSessions.get(0));
    }

    @Test
    public void userInfoTableStoresCorrectUserInformation() {
        saveUserOne();

        List<UserInfo> userInfo = db.query(UserInfoQueries.fetchUserInformationOfUser(playerUUID));
        List<UserInfo> expected = Collections.singletonList(new UserInfo(playerUUID, serverUUID, 1000L, false, false));

        assertEquals(expected, userInfo);
    }

    @Test
    public void userInfoTableUpdatesBanStatus() {
        saveUserOne();

        db.executeTransaction(new BanStatusTransaction(playerUUID, () -> true));

        List<UserInfo> userInfo = db.query(UserInfoQueries.fetchUserInformationOfUser(playerUUID));
        List<UserInfo> expected = Collections.singletonList(new UserInfo(playerUUID, serverUUID, 1000L, false, true));

        assertEquals(expected, userInfo);
    }

    @Test
    public void userInfoTableUpdatesOperatorStatus() {
        saveUserOne();

        db.executeTransaction(new OperatorStatusTransaction(playerUUID, true));

        List<UserInfo> userInfo = db.query(UserInfoQueries.fetchUserInformationOfUser(playerUUID));
        List<UserInfo> expected = Collections.singletonList(new UserInfo(playerUUID, serverUUID, 1000L, true, false));

        assertEquals(expected, userInfo);
    }

    @Test
    public void playerNameIsUpdatedWhenPlayerLogsIn() {
        saveUserOne();

        OptionalAssert.equals(playerUUID, db.query(UserIdentifierQueries.fetchPlayerUUIDOf(TestConstants.PLAYER_ONE_NAME)));

        // Updates the name
        db.executeTransaction(new PlayerRegisterTransaction(playerUUID, () -> 0, "NewName"));
        commitTest();

        assertFalse(db.query(UserIdentifierQueries.fetchPlayerUUIDOf(TestConstants.PLAYER_ONE_NAME)).isPresent());

        OptionalAssert.equals(playerUUID, db.query(UserIdentifierQueries.fetchPlayerUUIDOf("NewName")));
    }

    @Test
    public void testUsersTableKickSaving() {
        saveUserOne();
        OptionalAssert.equals(1, db.query(BaseUserQueries.fetchBaseUserOfPlayer(playerUUID)).map(BaseUser::getTimesKicked));

        int random = new Random().nextInt(20);

        for (int i = 0; i < random + 1; i++) {
            db.executeTransaction(new KickStoreTransaction(playerUUID));
        }
        commitTest();
        OptionalAssert.equals(random + 2, db.query(BaseUserQueries.fetchBaseUserOfPlayer(playerUUID)).map(BaseUser::getTimesKicked));
    }

    @Test
    public void testRemovalSingleUser() {
        saveUserTwo();

        db.executeTransaction(new PlayerServerRegisterTransaction(playerUUID, () -> 223456789L, "Test_name", serverUUID));
        saveTwoWorlds();

        Session session = new Session(playerUUID, serverUUID, 12345L, worlds[0], "SURVIVAL");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        execute(DataStoreQueries.storeSession(session));
        db.executeTransaction(new NicknameStoreTransaction(playerUUID, new Nickname("TestNick", System.currentTimeMillis(), serverUUID), (uuid, name) -> false /* Not cached */));
        saveGeoInfo(playerUUID, new GeoInfo("1.2.3.4", "TestLoc", 223456789L, "3"));

        assertTrue(db.query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));

        db.executeTransaction(new RemovePlayerTransaction(playerUUID));

        assertFalse(db.query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));
        assertFalse(db.query(PlayerFetchQueries.isPlayerRegisteredOnServer(playerUUID, serverUUID)));
        assertTrue(db.query(NicknameQueries.fetchNicknameDataOfPlayer(playerUUID)).isEmpty());
        assertTrue(db.query(GeoInfoQueries.fetchPlayerGeoInformation(playerUUID)).isEmpty());
        assertQueryIsEmpty(db, SessionQueries.fetchSessionsOfPlayer(playerUUID));
    }

    @Test
    public void testRemovalEverything() throws NoSuchAlgorithmException {
        saveAllData();

        db.executeTransaction(new RemoveEverythingTransaction());

        assertTrue(db.query(BaseUserQueries.fetchAllBaseUsers()).isEmpty());
        assertQueryIsEmpty(db, UserInfoQueries.fetchAllUserInformation());
        assertQueryIsEmpty(db, NicknameQueries.fetchAllNicknameData());
        assertQueryIsEmpty(db, GeoInfoQueries.fetchAllGeoInformation());
        assertTrue(db.query(SessionQueries.fetchAllSessions()).isEmpty());
        assertQueryIsEmpty(db, LargeFetchQueries.fetchAllCommandUsageData());
        assertQueryIsEmpty(db, LargeFetchQueries.fetchAllWorldNames());
        assertQueryIsEmpty(db, LargeFetchQueries.fetchAllTPSData());
        assertQueryIsEmpty(db, ServerQueries.fetchPlanServerInformation());
        assertQueryIsEmpty(db, PingQueries.fetchAllPingData());
        assertTrue(db.query(WebUserQueries.fetchAllPlanWebUsers()).isEmpty());
    }

    private <T extends Map> void assertQueryIsEmpty(Database database, Query<T> query) {
        assertTrue(database.query(query).isEmpty());
    }

    private void saveAllData() throws NoSuchAlgorithmException {
        saveUserOne();
        saveUserTwo();

        saveTwoWorlds();

        Session session = new Session(playerUUID, serverUUID, 12345L, worlds[0], "SURVIVAL");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());

        execute(DataStoreQueries.storeSession(session));
        db.executeTransaction(
                new NicknameStoreTransaction(playerUUID, new Nickname("TestNick", System.currentTimeMillis(), serverUUID), (uuid, name) -> false /* Not cached */)
        );
        saveGeoInfo(playerUUID, new GeoInfo("1.2.3.4", "TestLoc", 223456789L,
                new SHA256Hash("1.2.3.4").create()));

        assertTrue(db.query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));

        useCommand("plan");
        useCommand("plan");
        useCommand("tp");
        useCommand("help");
        useCommand("help");
        useCommand("help");

        List<TPS> expected = new ArrayList<>();
        Random r = new Random();
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        int availableProcessors = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
        double averageCPUUsage = operatingSystemMXBean.getSystemLoadAverage() / availableProcessors * 100.0;
        long usedMemory = 51231251254L;
        int entityCount = 6123;
        int chunksLoaded = 2134;
        long freeDiskSpace = new File("").getUsableSpace();
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded, freeDiskSpace));
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded, freeDiskSpace));
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded, freeDiskSpace));
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded, freeDiskSpace));
        for (TPS tps : expected) {
            execute(DataStoreQueries.storeTPS(serverUUID, tps));
        }

        db.executeTransaction(new PingStoreTransaction(
                playerUUID, serverUUID,
                Collections.singletonList(new DateObj<>(System.currentTimeMillis(), r.nextInt())))
        );

        WebUser webUser = new WebUser(TestConstants.PLAYER_ONE_NAME, "RandomGarbageBlah", 0);
        db.executeTransaction(new RegisterWebUserTransaction(webUser));
    }

    void saveGeoInfo(UUID uuid, GeoInfo geoInfo) {
        db.executeTransaction(new GeoInfoStoreTransaction(uuid, geoInfo));
    }

    @Test
    public void testSessionTableGetInfoOfServer() {
        saveUserOne();
        saveUserTwo();

        Session session = new Session(playerUUID, serverUUID, 12345L, worlds[0], "SURVIVAL");
        session.endSession(22345L);
        session.setWorldTimes(createWorldTimes());
        session.setPlayerKills(createKills());
        execute(DataStoreQueries.storeSession(session));

        commitTest();

        Map<UUID, List<Session>> sessions = db.query(SessionQueries.fetchSessionsOfServer(serverUUID));

        List<Session> sSessions = sessions.get(playerUUID);
        assertFalse(sessions.isEmpty());
        assertNotNull(sSessions);
        assertFalse(sSessions.isEmpty());
        assertEquals(session, sSessions.get(0));
    }

    @Test
    public void cleanDoesNotCleanActivePlayers() {
        saveUserOne();
        saveTwoWorlds();

        long sessionStart = System.currentTimeMillis();
        Session session = new Session(playerUUID, serverUUID, sessionStart, worlds[0], "SURVIVAL");
        session.endSession(sessionStart + 22345L);
        execute(DataStoreQueries.storeSession(session));

        new DBCleanTask(
                system.getConfigSystem().getConfig(),
                new Locale(),
                system.getDatabaseSystem(),
                system.getServerInfo(),
                new TestPluginLogger(),
                new ConsoleErrorLogger(new TestPluginLogger())
        ).cleanOldPlayers(db);

        Collection<BaseUser> found = db.query(BaseUserQueries.fetchServerBaseUsers(serverUUID));
        assertFalse("All users were deleted!! D:", found.isEmpty());
    }

    @Test
    public void cleanRemovesOnlyDuplicatedUserInfo() {
        // Store one duplicate
        db.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(DataStoreQueries.registerUserInfo(playerUUID, 0L, serverUUID));
                execute(DataStoreQueries.registerUserInfo(playerUUID, 0L, serverUUID));
                execute(DataStoreQueries.registerUserInfo(player2UUID, 0L, serverUUID));
            }
        });

        db.executeTransaction(new RemoveDuplicateUserInfoTransaction());

        List<UserInfo> found = db.query(UserInfoQueries.fetchUserInformationOfUser(playerUUID));
        assertEquals(
                Collections.singletonList(new UserInfo(playerUUID, serverUUID, 0, false, false)),
                found
        );

        List<UserInfo> found2 = db.query(UserInfoQueries.fetchUserInformationOfUser(player2UUID));
        assertEquals(
                Collections.singletonList(new UserInfo(player2UUID, serverUUID, 0, false, false)),
                found2
        );
    }

    @Test
    public void testKillTableGetKillsOfServer() {
        saveUserOne();
        saveUserTwo();

        Session session = createSession();
        List<PlayerKill> expected = createKills();
        session.setPlayerKills(expected);
        execute(DataStoreQueries.storeSession(session));

        commitTest();

        Map<UUID, List<Session>> sessions = db.query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        List<Session> savedSessions = sessions.get(serverUUID);
        assertNotNull(savedSessions);
        assertFalse(savedSessions.isEmpty());

        Session savedSession = savedSessions.get(0);
        assertNotNull(savedSession);

        List<PlayerKill> kills = savedSession.getPlayerKills();
        assertNotNull(kills);
        assertFalse(kills.isEmpty());
        assertEquals(expected, kills);
    }

    private Session createSession() {
        Session session = new Session(
                playerUUID,
                serverUUID,
                System.currentTimeMillis(),
                "world",
                GMTimes.getGMKeyArray()[0]
        );
        db.executeTransaction(new WorldNameStoreTransaction(serverUUID, "world"));
        session.endSession(System.currentTimeMillis() + 1L);
        return session;
    }

    @Test
    public void testBackupAndRestore() throws Exception {
        H2DB backup = dbSystem.getH2Factory().usingFile(temporaryFolder.newFile("backup.db"));
        backup.setTransactionExecutorServiceProvider(MoreExecutors::newDirectExecutorService);
        backup.init();

        saveAllData();

        backup.executeTransaction(new BackupCopyTransaction(db, backup));

        assertQueryResultIsEqual(db, backup, BaseUserQueries.fetchAllBaseUsers());
        assertQueryResultIsEqual(db, backup, UserInfoQueries.fetchAllUserInformation());
        assertQueryResultIsEqual(db, backup, NicknameQueries.fetchAllNicknameData());
        assertQueryResultIsEqual(db, backup, GeoInfoQueries.fetchAllGeoInformation());
        assertQueryResultIsEqual(db, backup, SessionQueries.fetchAllSessions());
        assertQueryResultIsEqual(db, backup, LargeFetchQueries.fetchAllCommandUsageData());
        assertQueryResultIsEqual(db, backup, LargeFetchQueries.fetchAllWorldNames());
        assertQueryResultIsEqual(db, backup, LargeFetchQueries.fetchAllTPSData());
        assertQueryResultIsEqual(db, backup, ServerQueries.fetchPlanServerInformation());
        assertQueryResultIsEqual(db, backup, WebUserQueries.fetchAllPlanWebUsers());
    }

    private <T> void assertQueryResultIsEqual(Database one, Database two, Query<T> query) {
        assertEquals(one.query(query), two.query(query));
    }

    @Test
    public void sessionWorldTimesAreFetchedCorrectly() {
        saveUserOne();
        WorldTimes worldTimes = createWorldTimes();
        Session session = new Session(1, playerUUID, serverUUID, 12345L, 23456L, 0, 0, 0);
        session.setWorldTimes(worldTimes);
        execute(DataStoreQueries.storeSession(session));

        // Fetch the session
        Map<UUID, List<Session>> sessions = db.query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        List<Session> serverSessions = sessions.get(serverUUID);
        assertNotNull(serverSessions);
        assertFalse(serverSessions.isEmpty());

        Session savedSession = serverSessions.get(0);
        assertEquals(worldTimes, savedSession.getUnsafe(SessionKeys.WORLD_TIMES));
    }

    @Test
    public void worldTimesAreSavedWithAllSessionSave() {
        saveTwoWorlds();
        saveUserOne();

        WorldTimes worldTimes = createWorldTimes();

        Session session = createSession();
        session.setWorldTimes(worldTimes);
        List<Session> sessions = new ArrayList<>();
        sessions.add(session);
        db.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(LargeStoreQueries.storeAllSessionsWithKillAndWorldData(sessions));
            }
        });

        Map<UUID, WorldTimes> saved = db.query(WorldTimesQueries.fetchPlayerWorldTimesOnServers(playerUUID));
        WorldTimes savedWorldTimes = saved.get(serverUUID);
        assertEquals(worldTimes, savedWorldTimes);
    }

    @Test
    public void worldTimesAreSavedWithSession() {
        saveTwoWorlds();
        saveUserOne();

        WorldTimes worldTimes = createWorldTimes();
        Session session = createSession();
        session.setWorldTimes(worldTimes);
        List<Session> sessions = new ArrayList<>();
        sessions.add(session);
        db.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(LargeStoreQueries.storeAllSessionsWithKillAndWorldData(sessions));
            }
        });

        List<Session> allSessions = db.query(SessionQueries.fetchAllSessions());

        assertEquals(worldTimes, allSessions.get(0).getUnsafe(SessionKeys.WORLD_TIMES));
    }

    @Test
    public void playersWorldTimesMatchTotal() {
        worldTimesAreSavedWithSession();
        WorldTimes worldTimesOfUser = db.query(WorldTimesQueries.fetchPlayerTotalWorldTimes(playerUUID));
        assertEquals(createWorldTimes(), worldTimesOfUser);
    }

    @Test
    public void serverWorldTimesMatchTotal() {
        worldTimesAreSavedWithSession();
        WorldTimes worldTimesOfServer = db.query(WorldTimesQueries.fetchServerTotalWorldTimes(serverUUID));
        assertEquals(createWorldTimes(), worldTimesOfServer);
    }

    @Test
    public void emptyServerWorldTimesIsEmpty() {
        WorldTimes worldTimesOfServer = db.query(WorldTimesQueries.fetchServerTotalWorldTimes(serverUUID));
        assertEquals(new WorldTimes(), worldTimesOfServer);
    }

    @Test
    public void playerIsRegisteredToUsersTable() {
        assertFalse(db.query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));
        db.executeTransaction(new PlayerRegisterTransaction(playerUUID, () -> 1000L, TestConstants.PLAYER_ONE_NAME));
        assertTrue(db.query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));
        assertFalse(db.query(PlayerFetchQueries.isPlayerRegisteredOnServer(playerUUID, serverUUID)));
    }

    @Test
    public void playerIsRegisteredToBothTables() {
        assertFalse(db.query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));
        assertFalse(db.query(PlayerFetchQueries.isPlayerRegisteredOnServer(playerUUID, serverUUID)));
        db.executeTransaction(new PlayerServerRegisterTransaction(playerUUID, () -> 1000L, TestConstants.PLAYER_ONE_NAME, serverUUID));
        assertTrue(db.query(PlayerFetchQueries.isPlayerRegistered(playerUUID)));
        assertTrue(db.query(PlayerFetchQueries.isPlayerRegisteredOnServer(playerUUID, serverUUID)));
    }

    @Test
    public void testNewContainerForPlayer() throws NoSuchAlgorithmException {
        saveAllData();

        long start = System.nanoTime();

        PlayerContainer container = db.query(ContainerFetchQueries.fetchPlayerContainer(playerUUID));

        assertTrue(container.supports(PlayerKeys.UUID));
        assertTrue(container.supports(PlayerKeys.REGISTERED));
        assertTrue(container.supports(PlayerKeys.NAME));
        assertTrue(container.supports(PlayerKeys.KICK_COUNT));

        assertTrue(container.supports(PlayerKeys.GEO_INFO));
        assertTrue(container.supports(PlayerKeys.NICKNAMES));

        assertTrue(container.supports(PlayerKeys.PER_SERVER));

        assertTrue(container.supports(PlayerKeys.OPERATOR));
        assertTrue(container.supports(PlayerKeys.BANNED));

        assertTrue(container.supports(PlayerKeys.SESSIONS));
        assertTrue(container.supports(PlayerKeys.WORLD_TIMES));
        assertTrue(container.supports(PlayerKeys.LAST_SEEN));
        assertTrue(container.supports(PlayerKeys.DEATH_COUNT));
        assertTrue(container.supports(PlayerKeys.MOB_KILL_COUNT));
        assertTrue(container.supports(PlayerKeys.PLAYER_KILLS));
        assertTrue(container.supports(PlayerKeys.PLAYER_KILL_COUNT));

        assertFalse(container.supports(PlayerKeys.ACTIVE_SESSION));
        container.putRawData(PlayerKeys.ACTIVE_SESSION, new Session(playerUUID, serverUUID, System.currentTimeMillis(), "TestWorld", "SURVIVAL"));
        assertTrue(container.supports(PlayerKeys.ACTIVE_SESSION));

        long end = System.nanoTime();

        assertFalse("Took too long: " + ((end - start) / 1000000.0) + "ms", end - start > TimeUnit.SECONDS.toNanos(1L));

        OptionalAssert.equals(playerUUID, container.getValue(PlayerKeys.UUID));
        OptionalAssert.equals(1000L, container.getValue(PlayerKeys.REGISTERED));
        OptionalAssert.equals(TestConstants.PLAYER_ONE_NAME, container.getValue(PlayerKeys.NAME));
        OptionalAssert.equals(1, container.getValue(PlayerKeys.KICK_COUNT));

        List<GeoInfo> expectedGeoInfo =
                Collections.singletonList(new GeoInfo("1.2.3.4", "TestLoc", 223456789, "ZpT4PJ9HbaMfXfa8xSADTn5X1CHSR7nTT0ntv8hKdkw="));
        OptionalAssert.equals(expectedGeoInfo, container.getValue(PlayerKeys.GEO_INFO));

        List<Nickname> expectedNicknames = Collections.singletonList(new Nickname("TestNick", -1, serverUUID));
        OptionalAssert.equals(expectedNicknames, container.getValue(PlayerKeys.NICKNAMES));

        OptionalAssert.equals(false, container.getValue(PlayerKeys.OPERATOR));
        OptionalAssert.equals(false, container.getValue(PlayerKeys.BANNED));

        // TODO Test rest
    }

    @Test
    public void playerContainerSupportsAllPlayerKeys() throws NoSuchAlgorithmException, IllegalAccessException {
        saveAllData();

        PlayerContainer playerContainer = db.query(ContainerFetchQueries.fetchPlayerContainer(playerUUID));
        // Active sessions are added after fetching
        playerContainer.putRawData(PlayerKeys.ACTIVE_SESSION, RandomData.randomSession());

        List<String> unsupported = new ArrayList<>();
        List<Key> keys = FieldFetcher.getPublicStaticFields(PlayerKeys.class, Key.class);
        for (Key key : keys) {
            if (!playerContainer.supports(key)) {
                unsupported.add(key.getKeyName());
            }
        }

        assertTrue("Some keys are not supported by PlayerContainer: PlayerKeys." + unsupported.toString(), unsupported.isEmpty());
    }

    @Test
    public void uninstallingServerStopsItFromBeingReturnedInServerQuery() {
        db.executeTransaction(new SetServerAsUninstalledTransaction(serverUUID));

        Optional<Server> found = db.query(ServerQueries.fetchServerMatchingIdentifier(serverUUID));
        assertFalse(found.isPresent());
    }

    @Test
    public void uninstallingServerStopsItFromBeingReturnedInServersQuery() {
        db.executeTransaction(new SetServerAsUninstalledTransaction(serverUUID));

        Collection<Server> found = db.query(ServerQueries.fetchPlanServerInformationCollection());
        assertTrue(found.isEmpty());
    }

    @Test
    public void serverContainerSupportsAllServerKeys() throws NoSuchAlgorithmException, IllegalAccessException {
        saveAllData();

        ServerContainer serverContainer = db.query(ContainerFetchQueries.fetchServerContainer(serverUUID));

        List<String> unsupported = new ArrayList<>();
        List<Key> keys = FieldFetcher.getPublicStaticFields(ServerKeys.class, Key.class);
        for (Key key : keys) {
            if (!serverContainer.supports(key)) {
                unsupported.add(key.getKeyName());
            }
        }

        assertTrue("Some keys are not supported by ServerContainer: ServerKeys." + unsupported.toString(), unsupported.isEmpty());
    }

    @Test
    public void analysisContainerSupportsAllAnalysisKeys() throws IllegalAccessException, NoSuchAlgorithmException {
        serverContainerSupportsAllServerKeys();
        AnalysisContainer.Factory factory = constructAnalysisContainerFactory();
        AnalysisContainer analysisContainer = factory.forServerContainer(
                db.query(ContainerFetchQueries.fetchServerContainer(serverUUID))
        );
        Collection<String> unsupported = new ArrayList<>();
        List<Key> keys = FieldFetcher.getPublicStaticFields(AnalysisKeys.class, Key.class);
        for (Key key : keys) {
            if (!analysisContainer.supports(key)) {
                unsupported.add(key.getKeyName());
            }
        }

        assertTrue("Some keys are not supported by AnalysisContainer: AnalysisKeys." + unsupported.toString(), unsupported.isEmpty());
    }

    private AnalysisContainer.Factory constructAnalysisContainerFactory() {
        return new AnalysisContainer.Factory(
                "1.0.0",
                system.getConfigSystem().getConfig(),
                system.getLocaleSystem().getLocale(),
                system.getConfigSystem().getTheme(),
                system.getServerInfo().getServerProperties(),
                system.getHtmlUtilities().getFormatters(),
                system.getHtmlUtilities().getGraphs(),
                system.getHtmlUtilities().getHtmlTables(),
                system.getHtmlUtilities().getAccordions(),
                system.getHtmlUtilities().getAnalysisPluginsTabContentCreator()
        );
    }

    @Test
    public void networkContainerSupportsAllNetworkKeys() throws IllegalAccessException, NoSuchAlgorithmException {
        serverContainerSupportsAllServerKeys();
        NetworkContainer networkContainer = db.query(ContainerFetchQueries.fetchNetworkContainer());

        List<String> unsupported = new ArrayList<>();
        List<Key> keys = FieldFetcher.getPublicStaticFields(NetworkKeys.class, Key.class);
        for (Key key : keys) {
            if (!networkContainer.supports(key)) {
                unsupported.add(key.getKeyName());
            }
        }

        assertTrue("Some keys are not supported by NetworkContainer: NetworkKeys." + unsupported.toString(), unsupported.isEmpty());
    }

    @Test
    public void testGetMatchingNames() {
        String exp1 = "TestName";
        String exp2 = "TestName2";

        UUID uuid1 = UUID.randomUUID();
        db.executeTransaction(new PlayerRegisterTransaction(uuid1, () -> 0L, exp1));
        db.executeTransaction(new PlayerRegisterTransaction(UUID.randomUUID(), () -> 0L, exp2));

        String searchFor = "testname";

        List<String> result = db.query(UserIdentifierQueries.fetchMatchingPlayerNames(searchFor));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(exp1, result.get(0));
        assertEquals(exp2, result.get(1));
    }

    @Test
    public void testGetMatchingNickNames() {
        UUID uuid = UUID.randomUUID();
        String userName = RandomData.randomString(10);

        db.executeTransaction(new PlayerRegisterTransaction(uuid, () -> 0L, userName));
        db.executeTransaction(new PlayerRegisterTransaction(playerUUID, () -> 1L, "Not random"));

        String nickname = "2" + RandomData.randomString(10);
        db.executeTransaction(new NicknameStoreTransaction(uuid, new Nickname(nickname, System.currentTimeMillis(), serverUUID), (u, name) -> false /* Not cached */));
        db.executeTransaction(new NicknameStoreTransaction(playerUUID, new Nickname("No nick", System.currentTimeMillis(), serverUUID), (u, name) -> true /* Cached */));

        String searchFor = "2";

        List<String> result = db.query(UserIdentifierQueries.fetchMatchingPlayerNames(searchFor));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userName, result.get(0));
    }

    @Test
    public void configIsStoredInTheDatabase() {
        PlanConfig config = system.getConfigSystem().getConfig();

        db.executeTransaction(new StoreConfigTransaction(serverUUID, config, System.currentTimeMillis()));

        Optional<Config> foundConfig = db.query(new NewerConfigQuery(serverUUID, 0));
        assertTrue(foundConfig.isPresent());
        assertEquals(config, foundConfig.get());
    }

    @Test
    public void unchangedConfigDoesNotUpdateInDatabase() {
        configIsStoredInTheDatabase();
        long savedMs = System.currentTimeMillis();

        PlanConfig config = system.getConfigSystem().getConfig();

        db.executeTransaction(new StoreConfigTransaction(serverUUID, config, System.currentTimeMillis()));

        assertFalse(db.query(new NewerConfigQuery(serverUUID, savedMs)).isPresent());
    }

    @Test
    public void indexCreationWorksWithoutErrors() {
        db.executeTransaction(new CreateIndexTransaction());
    }

    @Test
    public void playerMaxPeakIsCorrect() {
        List<TPS> tpsData = RandomData.randomTPS();

        for (TPS tps : tpsData) {
            db.executeTransaction(new TPSStoreTransaction(serverUUID, Collections.singletonList(tps)));
        }

        tpsData.sort(Comparator.comparingInt(TPS::getPlayers));
        int expected = tpsData.get(tpsData.size() - 1).getPlayers();
        int actual = db.query(TPSQueries.fetchAllTimePeakPlayerCount(serverUUID)).map(DateObj::getValue).orElse(-1);
        assertEquals("Wrong return value. " + tpsData.stream().map(TPS::getPlayers).collect(Collectors.toList()).toString(), expected, actual);
    }

    @Test
    public void playerCountForServersIsCorrect() {
        Map<UUID, Integer> expected = Collections.singletonMap(serverUUID, 1);
        saveUserOne();

        Map<UUID, Integer> result = db.query(ServerAggregateQueries.serverUserCounts());
        assertEquals(expected, result);
    }

    private void executeTransactions(Transaction... transactions) {
        for (Transaction transaction : transactions) {
            db.executeTransaction(transaction);
        }
    }

    @Test
    public void baseUsersQueryDoesNotReturnDuplicatePlayers() {
        db.executeTransaction(TestData.storeServers());
        executeTransactions(TestData.storePlayerOneData());
        executeTransactions(TestData.storePlayerTwoData());

        Collection<BaseUser> expected = new HashSet<>(Arrays.asList(TestData.getPlayerBaseUser(), TestData.getPlayer2BaseUser()));
        Collection<BaseUser> result = db.query(BaseUserQueries.fetchServerBaseUsers(TestConstants.SERVER_UUID));

        assertEquals(expected, result);

        result = db.query(BaseUserQueries.fetchServerBaseUsers(TestConstants.SERVER_TWO_UUID));

        assertEquals(expected, result);
    }

    @Test
    public void serverPlayerContainersQueryDoesNotReturnDuplicatePlayers() {
        db.executeTransaction(TestData.storeServers());
        executeTransactions(TestData.storePlayerOneData());
        executeTransactions(TestData.storePlayerTwoData());

        List<UUID> expected = Arrays.asList(playerUUID, player2UUID);
        Collections.sort(expected);

        Collection<UUID> result = db.query(new ServerPlayerContainersQuery(TestConstants.SERVER_UUID))
                .stream().map(player -> player.getUnsafe(PlayerKeys.UUID))
                .sorted()
                .collect(Collectors.toList());

        assertEquals(expected, result);
    }

    @Test
    public void allPlayerContainersQueryDoesNotReturnDuplicatePlayers() {
        db.executeTransaction(TestData.storeServers());
        executeTransactions(TestData.storePlayerOneData());
        executeTransactions(TestData.storePlayerTwoData());

        List<UUID> expected = Arrays.asList(playerUUID, player2UUID);
        Collections.sort(expected);

        Collection<UUID> result = db.query(new AllPlayerContainersQuery())
                .stream().map(player -> player.getUnsafe(PlayerKeys.UUID))
                .sorted()
                .collect(Collectors.toList());

        assertEquals(expected, result);
    }

    // This test is against issue https://github.com/Rsl1122/Plan-PlayerAnalytics/issues/956
    @Test
    public void analysisContainerPlayerNamesAreCollectedFromBaseUsersCorrectly() {
        db.executeTransaction(TestData.storeServers());
        executeTransactions(TestData.storePlayerOneData());
        executeTransactions(TestData.storePlayerTwoData());

        BaseUser playerBaseUser = TestData.getPlayerBaseUser();
        BaseUser player2BaseUser = TestData.getPlayer2BaseUser();

        AnalysisContainer.Factory factory = constructAnalysisContainerFactory();
        AnalysisContainer analysisContainer = factory.forServerContainer(
                db.query(ContainerFetchQueries.fetchServerContainer(TestConstants.SERVER_UUID))
        );

        Map<UUID, String> expected = new HashMap<>();
        expected.put(playerBaseUser.getUuid(), playerBaseUser.getName());
        expected.put(player2BaseUser.getUuid(), player2BaseUser.getName());
        Map<UUID, String> result = analysisContainer.getValue(AnalysisKeys.PLAYER_NAMES).orElseThrow(AssertionError::new);

        assertEquals(expected, result);
    }

    @Test
    public void extensionPlayerValuesAreStored() {
        ExtensionServiceImplementation extensionService = (ExtensionServiceImplementation) system.getExtensionService();

        extensionService.register(new PlayerExtension());
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        Map<UUID, List<ExtensionPlayerData>> playerDataByServerUUID = db.query(new ExtensionPlayerDataQuery(playerUUID));
        List<ExtensionPlayerData> ofServer = playerDataByServerUUID.get(serverUUID);
        assertNotNull(ofServer);
        assertFalse(ofServer.isEmpty());

        ExtensionPlayerData extensionPlayerData = ofServer.get(0);
        List<ExtensionTabData> tabs = extensionPlayerData.getTabs();
        assertEquals(1, tabs.size()); // No tab defined, should contain 1 tab
        ExtensionTabData tabData = tabs.get(0);

        OptionalAssert.equals("5", tabData.getNumber("value").map(data -> data.getFormattedValue(Object::toString)));
        OptionalAssert.equals("No", tabData.getBoolean("boolVal").map(ExtensionBooleanData::getFormattedValue));
        OptionalAssert.equals("0.5", tabData.getDouble("doubleVal").map(data -> data.getFormattedValue(Object::toString)));
        OptionalAssert.equals("0.5", tabData.getPercentage("percentageVal").map(data -> data.getFormattedValue(Object::toString)));
        OptionalAssert.equals("Something", tabData.getString("stringVal").map(ExtensionStringData::getFormattedValue));
    }

    @Test
    public void extensionServerValuesAreStored() {
        ExtensionServiceImplementation extensionService = (ExtensionServiceImplementation) system.getExtensionService();

        extensionService.register(new ServerExtension());
        extensionService.updateServerValues(CallEvents.SERVER_EXTENSION_REGISTER);

        List<ExtensionServerData> ofServer = db.query(new ExtensionServerDataQuery(serverUUID));
        assertFalse(ofServer.isEmpty());

        ExtensionServerData extensionServerData = ofServer.get(0);
        List<ExtensionTabData> tabs = extensionServerData.getTabs();
        assertEquals(1, tabs.size()); // No tab defined, should contain 1 tab
        ExtensionTabData tabData = tabs.get(0);

        OptionalAssert.equals("5", tabData.getNumber("value").map(data -> data.getFormattedValue(Object::toString)));
        OptionalAssert.equals("No", tabData.getBoolean("boolVal").map(ExtensionBooleanData::getFormattedValue));
        OptionalAssert.equals("0.5", tabData.getDouble("doubleVal").map(data -> data.getFormattedValue(Object::toString)));
        OptionalAssert.equals("0.5", tabData.getPercentage("percentageVal").map(data -> data.getFormattedValue(Object::toString)));
        OptionalAssert.equals("Something", tabData.getString("stringVal").map(ExtensionStringData::getFormattedValue));
    }

    @Test
    public void extensionServerAggregateQueriesWork() {
        ExtensionServiceImplementation extensionService = (ExtensionServiceImplementation) system.getExtensionService();

        extensionService.register(new PlayerExtension());
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        List<ExtensionServerData> ofServer = db.query(new ExtensionServerDataQuery(serverUUID));
        assertFalse(ofServer.isEmpty());

        ExtensionServerData extensionServerData = ofServer.get(0);
        List<ExtensionTabData> tabs = extensionServerData.getTabs();
        assertEquals(1, tabs.size()); // No tab defined, should contain 1 tab
        ExtensionTabData tabData = tabs.get(0);

        System.out.println(tabData.getValueOrder());

        OptionalAssert.equals("0.0", tabData.getPercentage("boolVal_aggregate").map(data -> data.getFormattedValue(Objects::toString)));
        OptionalAssert.equals("0.5", tabData.getPercentage("percentageVal_avg").map(data -> data.getFormattedValue(Objects::toString)));
        OptionalAssert.equals("0.5", tabData.getDouble("doubleVal_avg").map(data -> data.getFormattedValue(Objects::toString)));
        OptionalAssert.equals("0.5", tabData.getDouble("doubleVal_total").map(data -> data.getFormattedValue(Objects::toString)));
        OptionalAssert.equals("5", tabData.getNumber("value_avg").map(data -> data.getFormattedValue(Objects::toString)));
        OptionalAssert.equals("5", tabData.getNumber("value_total").map(data -> data.getFormattedValue(Objects::toString)));
    }

    @Test
    public void unsatisfiedConditionalResultsAreCleaned() throws ExecutionException, InterruptedException {
        ExtensionServiceImplementation extensionService = (ExtensionServiceImplementation) system.getExtensionService();

        extensionService.register(new ConditionalExtension());

        ConditionalExtension.condition = true;
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        // Check that the wanted data exists
        checkThatDataExists(ConditionalExtension.condition);

        // Reverse condition
        ConditionalExtension.condition = false;
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        db.executeTransaction(new RemoveUnsatisfiedConditionalResultsTransaction());

        // Check that the wanted data exists
        checkThatDataExists(ConditionalExtension.condition);

        // Reverse condition
        ConditionalExtension.condition = false;
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        db.executeTransaction(new RemoveUnsatisfiedConditionalResultsTransaction());

        // Check that the wanted data exists
        checkThatDataExists(ConditionalExtension.condition);
    }

    private void checkThatDataExists(boolean condition) {
        if (condition) { // Condition is true, conditional values exist
            List<ExtensionPlayerData> ofServer = db.query(new ExtensionPlayerDataQuery(playerUUID)).get(serverUUID);
            assertTrue("There was no data left", ofServer != null && !ofServer.isEmpty() && !ofServer.get(0).getTabs().isEmpty());

            ExtensionTabData tabData = ofServer.get(0).getTabs().get(0);
            OptionalAssert.equals("Yes", tabData.getBoolean("isCondition").map(ExtensionBooleanData::getFormattedValue));
            OptionalAssert.equals("Conditional", tabData.getString("conditionalValue").map(ExtensionStringData::getFormattedValue));
            OptionalAssert.equals("unconditional", tabData.getString("unconditional").map(ExtensionStringData::getFormattedValue)); // Was not removed
            assertFalse("Value was not removed: reversedConditionalValue", tabData.getString("reversedConditionalValue").isPresent());
        } else { // Condition is false, reversed conditional values exist
            List<ExtensionPlayerData> ofServer = db.query(new ExtensionPlayerDataQuery(playerUUID)).get(serverUUID);
            assertTrue("There was no data left", ofServer != null && !ofServer.isEmpty() && !ofServer.get(0).getTabs().isEmpty());
            ExtensionTabData tabData = ofServer.get(0).getTabs().get(0);
            OptionalAssert.equals("No", tabData.getBoolean("isCondition").map(ExtensionBooleanData::getFormattedValue));
            OptionalAssert.equals("Reversed", tabData.getString("reversedConditionalValue").map(ExtensionStringData::getFormattedValue));
            OptionalAssert.equals("unconditional", tabData.getString("unconditional").map(ExtensionStringData::getFormattedValue)); // Was not removed
            assertFalse("Value was not removed: conditionalValue", tabData.getString("conditionalValue").isPresent());
        }
    }

    @Test
    public void playerTableValuesAreInserted() {
        ExtensionServiceImplementation extensionService = (ExtensionServiceImplementation) system.getExtensionService();

        extensionService.register(new ConditionalExtension());
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);
        extensionService.updatePlayerValues(playerUUID, TestConstants.PLAYER_ONE_NAME, CallEvents.MANUAL);

        // TODO query
    }

    @PluginInfo(name = "ConditionalExtension")
    public static class ConditionalExtension implements DataExtension {

        static boolean condition = true;

        @BooleanProvider(text = "a boolean", conditionName = "condition")
        public boolean isCondition(UUID playerUUID) {
            return condition;
        }

        @StringProvider(text = "Conditional Value")
        @Conditional("condition")
        public String conditionalValue(UUID playerUUID) {
            return "Conditional";
        }

        @StringProvider(text = "Reversed Conditional Value")
        @Conditional(value = "condition", negated = true)
        public String reversedConditionalValue(UUID playerUUID) {
            return "Reversed";
        }

        @StringProvider(text = "Unconditional")
        public String unconditional(UUID playerUUID) {
            return "unconditional";
        }
    }

    @PluginInfo(name = "ServerExtension")
    public class ServerExtension implements DataExtension {
        @NumberProvider(text = "a number")
        public long value() {
            return 5L;
        }

        @BooleanProvider(text = "a boolean")
        public boolean boolVal() {
            return false;
        }

        @DoubleProvider(text = "a double")
        public double doubleVal() {
            return 0.5;
        }

        @PercentageProvider(text = "a percentage")
        public double percentageVal() {
            return 0.5;
        }

        @StringProvider(text = "a string")
        public String stringVal() {
            return "Something";
        }
    }

    @PluginInfo(name = "PlayerExtension")
    public class PlayerExtension implements DataExtension {
        @NumberProvider(text = "a number")
        public long value(UUID playerUUD) {
            return 5L;
        }

        @BooleanProvider(text = "a boolean")
        public boolean boolVal(UUID playerUUID) {
            return false;
        }

        @DoubleProvider(text = "a double")
        public double doubleVal(UUID playerUUID) {
            return 0.5;
        }

        @PercentageProvider(text = "a percentage")
        public double percentageVal(UUID playerUUID) {
            return 0.5;
        }

        @StringProvider(text = "a string")
        public String stringVal(UUID playerUUID) {
            return "Something";
        }
    }

    @PluginInfo(name = "TableExtension")
    public class TableExtension implements DataExtension {
        @TableProvider(tableColor = Color.AMBER)
        public Table table(UUID playerUUID) {
            return Table.builder()
                    .columnOne("first", Icon.called("gavel").of(Color.AMBER).build())
                    .columnTwo("second", Icon.called("what").of(Color.BROWN).build()) // Colors are ignored
                    .columnThree("third", null)                  // Can handle improper icons
                    .columnFive("five", Icon.called("").build()) // Can handle null column in between and ignore the next column
                    .addRow("value", 3, 0.5, 400L)               // Can handle too many row values
                    .build();
        }
    }
}
