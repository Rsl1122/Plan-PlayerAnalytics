/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package utilities.mocks;

import com.djrapitops.plan.PlanVelocity;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.console.TestPluginLogger;
import com.djrapitops.plugin.logging.debug.CombineDebugLogger;
import com.djrapitops.plugin.logging.debug.DebugLogger;
import com.djrapitops.plugin.logging.debug.MemoryDebugLogger;
import com.djrapitops.plugin.logging.error.ConsoleErrorLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.thread.ThreadRunnableFactory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.mockito.Mockito;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Mocking Utility for Velocity version of Plan (PlanVelocity).
 *
 * @author Rsl1122
 */
public class PlanVelocityMocker extends Mocker {

    private PlanVelocity planMock;

    private PlanVelocityMocker() {
    }

    public static PlanVelocityMocker setUp() {
        return new PlanVelocityMocker().mockPlugin();
    }

    private PlanVelocityMocker mockPlugin() {
        planMock = Mockito.mock(PlanVelocity.class);
        super.planMock = planMock;

        doReturn(new ColorScheme("§1", "§2", "§3")).when(planMock).getColorScheme();
        doReturn("1.0.0").when(planMock).getVersion();

        ThreadRunnableFactory runnableFactory = new ThreadRunnableFactory();
        PluginLogger testPluginLogger = new TestPluginLogger();
        DebugLogger debugLogger = new CombineDebugLogger(new MemoryDebugLogger());
        ErrorHandler consoleErrorLogger = new ConsoleErrorLogger(testPluginLogger);
        Timings timings = new Timings(debugLogger);

        doReturn(runnableFactory).when(planMock).getRunnableFactory();
        doReturn(testPluginLogger).when(planMock).getPluginLogger();
        doReturn(debugLogger).when(planMock).getDebugLogger();
        doReturn(consoleErrorLogger).when(planMock).getErrorHandler();
        doReturn(timings).when(planMock).getTimings();

        return this;
    }

    public PlanVelocityMocker withDataFolder(File tempFolder) {
        when(planMock.getDataFolder()).thenReturn(tempFolder);
        return this;
    }

    public PlanVelocityMocker withResourceFetchingFromJar() throws Exception {
        withPluginFiles();
        return this;
    }

    @Deprecated
    public PlanVelocityMocker withLogging() {
        return this;
    }

    public PlanVelocityMocker withProxy() {
        ProxyServer server = Mockito.mock(ProxyServer.class);

        InetSocketAddress ip = new InetSocketAddress(25565);

        doReturn(new ArrayList<>()).when(server).getAllServers();
        doReturn(ip).when(server).getBoundAddress();

        doReturn(server).when(planMock).getProxy();
        return this;
    }

    public PlanVelocity getPlanMock() {
        return planMock;
    }
}
