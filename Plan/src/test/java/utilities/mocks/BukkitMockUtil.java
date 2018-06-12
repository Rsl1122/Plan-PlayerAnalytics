/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package utilities.mocks;

import com.djrapitops.plan.Plan;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.task.ThreadRunnable;
import org.bukkit.Server;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitScheduler;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.Teardown;
import utilities.TestConstants;
import utilities.mocks.objects.FakeConsoleCmdSender;
import utilities.mocks.objects.TestLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;

/**
 * Mocking Utility for Bukkit version of Plan.
 *
 * @author Rsl1122
 */
public class BukkitMockUtil extends MockUtil {

    private Plan planMock;

    private BukkitMockUtil() {
    }

    public static BukkitMockUtil setUp() {
        RunnableFactory.activateTestMode();
        Teardown.resetSettingsTempValues();
        return new BukkitMockUtil().mockPlugin();
    }

    private BukkitMockUtil mockPlugin() {
        planMock = Mockito.mock(Plan.class);
        super.planMock = planMock;
        StaticHolder.register(Plan.class, planMock);
        StaticHolder.register(planMock);

        StaticHolder.saveInstance(MockitoJUnitRunner.class, Plan.class);
        StaticHolder.saveInstance(ThreadRunnable.class, Plan.class);

        doCallRealMethod().when(planMock).getVersion();
        doCallRealMethod().when(planMock).getColorScheme();

        return this;
    }

    public BukkitMockUtil withDataFolder(File tempFolder) {
        doReturn(tempFolder).when(planMock).getDataFolder();
        return this;
    }

    public BukkitMockUtil withLogging() {
        doCallRealMethod().when(planMock).log(Mockito.anyString(), Mockito.anyString());
        TestLogger testLogger = new TestLogger();
        doReturn(testLogger).when(planMock).getLogger();
        return this;
    }


    public BukkitMockUtil withPluginDescription() {
        try {
            File pluginYml = getFile("/plugin.yml");
            PluginDescriptionFile description = new PluginDescriptionFile(new FileInputStream(pluginYml));
            doReturn(description).when(planMock).getDescription();
        } catch (FileNotFoundException | InvalidDescriptionException e) {
            System.out.println("Error while setting plugin description");
        }
        return this;
    }

    public BukkitMockUtil withResourceFetchingFromJar() throws Exception {
        withPluginFiles();
        return this;
    }

    public BukkitMockUtil withServer() {
        Server serverMock = Mockito.mock(Server.class);
        doReturn("").when(serverMock).getIp();
        doReturn("Bukkit").when(serverMock).getName();
        doReturn("Bukkit").when(serverMock).getServerName();
        doReturn(25565).when(serverMock).getPort();
        doReturn("1.12.2").when(serverMock).getVersion();
        doReturn("32423").when(serverMock).getBukkitVersion();
        doReturn(TestConstants.BUKKIT_MAX_PLAYERS).when(serverMock).getMaxPlayers();
        FakeConsoleCmdSender sender = new FakeConsoleCmdSender();
        doReturn(sender).when(serverMock).getConsoleSender();

        BukkitScheduler bukkitScheduler = Mockito.mock(BukkitScheduler.class);
        doReturn(bukkitScheduler).when(serverMock).getScheduler();

        doReturn(serverMock).when(planMock).getServer();
        return this;
    }

    public Plan getPlanMock() {
        return planMock;
    }
}
