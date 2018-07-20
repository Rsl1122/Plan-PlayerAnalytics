/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.common.system.webserver.pages.parsing;

import com.djrapitops.plan.common.api.exceptions.ParseException;
import com.djrapitops.plan.common.data.store.containers.NetworkContainer;
import com.djrapitops.plan.common.data.store.mutators.formatting.PlaceholderReplacer;
import com.djrapitops.plan.common.system.database.databases.Database;
import com.djrapitops.plan.common.system.webserver.response.cache.PageId;
import com.djrapitops.plan.common.system.webserver.response.cache.ResponseCache;
import com.djrapitops.plan.common.system.webserver.response.pages.parts.NetworkPageContent;
import com.djrapitops.plan.common.utilities.file.FileUtil;

import static com.djrapitops.plan.common.data.store.keys.NetworkKeys.*;

/**
 * Html String parser for /network page.
 *
 * @author Rsl1122
 */
public class NetworkPage implements Page {

    @Override
    public String toHtml() throws ParseException {
        try {
            Database database = Database.getActive();
            NetworkContainer networkContainer = database.fetch().getNetworkContainer();

            PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer();
            placeholderReplacer.addAllPlaceholdersFrom(networkContainer,
                    VERSION, NETWORK_NAME, TIME_ZONE,
                    PLAYERS_ONLINE, PLAYERS_ONLINE_SERIES, PLAYERS_TOTAL, PLAYERS_GRAPH_COLOR,
                    REFRESH_TIME_F, RECENT_PEAK_TIME_F, ALL_TIME_PEAK_TIME_F,
                    PLAYERS_ALL_TIME_PEAK, PLAYERS_RECENT_PEAK,
                    PLAYERS_DAY, PLAYERS_WEEK, PLAYERS_MONTH,
                    PLAYERS_NEW_DAY, PLAYERS_NEW_WEEK, PLAYERS_NEW_MONTH,
                    WORLD_MAP_SERIES, WORLD_MAP_HIGH_COLOR, WORLD_MAP_LOW_COLOR,
                    COUNTRY_CATEGORIES, COUNTRY_SERIES,
                    HEALTH_INDEX, HEALTH_NOTES,
                    ACTIVITY_PIE_SERIES, ACTIVITY_STACK_SERIES, ACTIVITY_STACK_CATEGORIES
            );
            NetworkPageContent networkPageContent = (NetworkPageContent)
                    ResponseCache.loadResponse(PageId.NETWORK_CONTENT.id(), NetworkPageContent::new);
            placeholderReplacer.put("tabContentServers", networkPageContent.getContents());

            return placeholderReplacer.apply(FileUtil.getStringFromResource("web/network.html"));
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }
}