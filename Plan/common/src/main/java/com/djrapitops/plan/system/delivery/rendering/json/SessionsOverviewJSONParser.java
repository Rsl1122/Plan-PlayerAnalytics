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
package com.djrapitops.plan.system.delivery.rendering.json;

import com.djrapitops.plan.system.delivery.domain.mutators.TPSMutator;
import com.djrapitops.plan.system.gathering.domain.GMTimes;
import com.djrapitops.plan.system.gathering.domain.TPS;
import com.djrapitops.plan.system.storage.database.DBSystem;
import com.djrapitops.plan.system.storage.database.Database;
import com.djrapitops.plan.system.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.system.storage.database.queries.objects.TPSQueries;
import com.djrapitops.plan.system.storage.database.queries.objects.WorldTimesQueries;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import org.apache.commons.text.WordUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Parses JSON payload for /server-page Sessions tab.
 *
 * @author Rsl1122
 */
@Singleton
public class SessionsOverviewJSONParser implements ServerTabJSONParser<Map<String, Object>> {

    private DBSystem dbSystem;

    private Formatter<Long> timeAmount;
    private Formatter<Double> percentage;

    @Inject
    public SessionsOverviewJSONParser(
            DBSystem dbSystem,
            Formatters formatters
    ) {
        this.dbSystem = dbSystem;

        timeAmount = formatters.timeAmount();
        percentage = formatters.percentage();
    }

    public Map<String, Object> createJSONAsMap(UUID serverUUID) {
        return Collections.singletonMap("insights", createInsightsMap(serverUUID));
    }

    private Map<String, Object> createInsightsMap(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);

        List<TPS> tpsData = db.query(TPSQueries.fetchTPSDataOfServer(monthAgo, now, serverUUID));
        TPSMutator tpsMutator = new TPSMutator(tpsData);

        Map<String, Object> insights = new HashMap<>();

        long uptime = TimeUnit.DAYS.toMillis(30L) - tpsMutator.serverDownTime();
        long occupied = tpsMutator.serverOccupiedTime();
        insights.put("server_occupied", timeAmount.apply(occupied));
        insights.put("server_occupied_perc", uptime != 0 ? percentage.apply(1.0 * occupied / uptime) : "-");

        Long playtime = db.query(SessionQueries.playtime(monthAgo, now, serverUUID));
        Long afkTime = db.query(SessionQueries.afkTime(monthAgo, now, serverUUID));
        insights.put("total_playtime", timeAmount.apply(playtime));
        insights.put("afk_time", timeAmount.apply(afkTime));
        insights.put("afk_time_perc", playtime != 0 ? percentage.apply(1.0 * afkTime / playtime) : "-");

        GMTimes gmTimes = db.query(WorldTimesQueries.fetchGMTimes(monthAgo, now, serverUUID));
        Optional<String> mostUsedGameMode = gmTimes.getMostUsedGameMode();
        Long longestGMTime = mostUsedGameMode.map(gmTimes::getTime).orElse(-1L);
        insights.put("most_active_gamemode", mostUsedGameMode.map(WordUtils::capitalizeFully).orElse("Not Known"));
        insights.put("most_active_gamemode_perc", playtime != 0 ? percentage.apply(1.0 * longestGMTime / playtime) : "-");

        return insights;
    }
}