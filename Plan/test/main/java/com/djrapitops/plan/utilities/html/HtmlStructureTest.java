package main.java.com.djrapitops.plan.utilities.html;

import main.java.com.djrapitops.plan.data.Session;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.RandomData;
import test.java.utils.TestInit;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class HtmlStructureTest {

    private Map<String, List<Session>> sessions = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        TestInit.init();

        for (int i = 0; i < RandomData.randomInt(0, 5); i++) {
            sessions.put(RandomData.randomString(10), RandomData.randomSessions());
        }
    }

    @Test
    public void createServerOverviewColumn() throws Exception {
        String serverOverviewColumn = HtmlStructure.createServerOverviewColumn(sessions);

        int opened = StringUtils.countMatches(serverOverviewColumn, "<div");
        int closed = StringUtils.countMatches(serverOverviewColumn, "</div");

        assertEquals(opened, closed);
    }

    @Test
    public void createSessionsTabContent() throws Exception {
        List<Session> allSessions = sessions.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        String[] sessionsTab = HtmlStructure.createSessionsTabContent(sessions, allSessions);

        int opened = StringUtils.countMatches(sessionsTab[0], "<div");
        int closed = StringUtils.countMatches(sessionsTab[0], "</div");

        assertEquals(opened, closed);
    }
}