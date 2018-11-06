/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.database;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.database.databases.sql.H2DB;
import com.djrapitops.plan.system.database.databases.sql.MySQLDB;
import com.djrapitops.plan.system.database.databases.sql.SQLiteDB;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Bukkit Database system that initializes SQLite and MySQL database objects.
 *
 * @author Rsl1122
 */
@Singleton
public class BukkitDBSystem extends DBSystem {

    private final PlanConfig config;

    @Inject
    public BukkitDBSystem(
            Locale locale,
            MySQLDB mySQLDB,
            SQLiteDB.Factory sqLiteDB,
            H2DB.Factory h2DB,
            PlanConfig config,
            PluginLogger logger,
            Timings timings,
            ErrorHandler errorHandler
    ) {
        super(locale, sqLiteDB, h2DB, logger, timings, errorHandler);
        this.config = config;

        databases.add(mySQLDB);
        databases.add(h2DB.usingDefaultFile());
        databases.add(sqLiteDB.usingDefaultFile());
    }

    @Override
    public void enable() throws EnableException {
        String dbType = config.getString(Settings.DB_TYPE).toLowerCase().trim();
        db = getActiveDatabaseByName(dbType);
        super.enable();
    }
}
