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
package com.djrapitops.plan.storage.database;

import com.djrapitops.plan.PlanSystem;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import utilities.DBPreparer;
import utilities.RandomData;
import utilities.mocks.PluginMockComponent;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Test for the H2 database
 *
 * @author Rsl1122, Fuzzlemann
 * @see SQLiteTest
 */
@RunWith(JUnitPlatform.class)
@ExtendWith(MockitoExtension.class)
public class H2Test implements DatabaseTest {

    private static final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);

    private static PlanSystem system;
    private static Database database;

    @BeforeAll
    static void setupDatabase(@TempDir Path temp) throws Exception {
        system = new PluginMockComponent(temp).getPlanSystem();
        database = new DBPreparer(system, TEST_PORT_NUMBER).prepareH2()
                .orElseThrow(IllegalStateException::new);
    }

    @AfterAll
    static void disableSystem() {
        if (database != null) database.close();
        system.disable();
    }

    @Override
    public Database db() {
        return database;
    }

    @Override
    public UUID serverUUID() {
        return system.getServerInfo().getServerUUID();
    }

    @Override
    public PlanSystem system() {
        return system;
    }
}
