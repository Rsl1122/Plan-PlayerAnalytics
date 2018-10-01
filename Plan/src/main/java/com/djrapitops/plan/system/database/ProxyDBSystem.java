/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.databases.sql.MySQLDB;
import com.djrapitops.plan.system.locale.Locale;

import java.util.function.Supplier;

/**
 * Bungee Database system that initializes MySQL object.
 *
 * @author Rsl1122
 */
public class ProxyDBSystem extends DBSystem {

    public ProxyDBSystem(Supplier<Locale> locale) {
        super(locale);
    }

    @Override
    protected void initDatabase() throws DBInitException {
        db = new MySQLDB(locale);
        databases.add(db);
        db.init();
    }
}
