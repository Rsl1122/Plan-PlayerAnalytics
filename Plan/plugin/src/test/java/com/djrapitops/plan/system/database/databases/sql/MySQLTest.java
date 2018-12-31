package com.djrapitops.plan.system.database.databases.sql;

import com.djrapitops.plan.system.database.databases.sql.patches.Patch;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DatabaseSettings;
import org.junit.BeforeClass;
import utilities.CIProperties;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assume.assumeTrue;

/**
 * Tests for {@link com.djrapitops.plan.system.database.databases.sql.MySQLDB}.
 * <p>
 * These settings assume Travis CI environment with MySQL service running.
 * 'Plan' database should be created before the test.
 *
 * @author Rsl1122
 */
public class MySQLTest extends CommonDBTest {

    @BeforeClass
    public static void setUpDatabase() throws Exception {
        boolean isTravis = Boolean.parseBoolean(System.getenv(CIProperties.IS_TRAVIS));
        assumeTrue(isTravis);

        PlanConfig config = component.getPlanSystem().getConfigSystem().getConfig();
        config.set(DatabaseSettings.MYSQL_DATABASE, "Plan");
        config.set(DatabaseSettings.MYSQL_USER, "travis");
        config.set(DatabaseSettings.MYSQL_PASS, "");
        config.set(DatabaseSettings.MYSQL_HOST, "127.0.0.1");
        config.set(DatabaseSettings.TYPE, "MySQL");

        handleSetup("MySQL");
        clearDatabase();
    }

    private static void clearDatabase() {
        List<String> tables = db.query(new QueryAllStatement<List<String>>("SELECT table_name" +
                " FROM information_schema.tables") {
            @Override
            public List<String> processResults(ResultSet resultSet) throws SQLException {
                List<String> names = new ArrayList<>();
                while (resultSet.next()) {
                    names.add(resultSet.getString("table_name"));
                }
                return names;
            }
        });

        new Patch(db) {
            @Override
            public boolean hasBeenApplied() {
                return false;
            }

            @Override
            public void apply() {
                for (String tableName : tables) {
                    dropTable(tableName);
                }
            }
        }.apply();
    }
}
