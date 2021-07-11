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
package com.djrapitops.plan.settings.locale.lang;

/**
 * Lang enum for all text included in the javascript files.
 *
 * @author AuroraLS3
 */
public enum JSLang implements Lang {

    TEXT_PREDICTED_RETENTION("", "This value is a prediction based on previous players"),
    TEXT_NO_SERVERS("", "No servers found in the database"),
    TEXT_SERVER_INSTRUCTIONS("", "It appears that Plan is not installed on any game servers or not connected to the same database. See <a href=\"https://github.com/plan-player-analytics/Plan/wiki\">wiki</a> for Network tutorial."),
    TEXT_NO_SERVER("", "No server to display online activity for"),
    LABEL_REGISTERED_PLAYERS("", "Registered Players"),
    LINK_SERVER_ANALYSIS("", "Server Analysis"),
    LINK_QUICK_VIEW("", "Quick view"),
    TEXT_FIRST_SESSION("", "First session"),
    LABEL_SESSION_ENDED("", " Ended"),
    LINK_PLAYER_PAGE("", "Player Page"),
    LABEL_NO_SESSION_KILLS("", "None"),
    UNIT_ENTITIES("", "Entities"),
    UNIT_CHUNKS("", "Chunks"),
    LABEL_RELATIVE_JOIN_ACTIVITY("", "Relative Join Activity"),
    LABEL_DAY_OF_WEEK("", "Day of the Week"),
    LABEL_WEEK_DAYS("", "'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'"),

    QUERY_ARE_ACTIVITY_GROUP("", "are in Activity Groups"),
    QUERY_JOINED_WITH_ADDRESS("", "joined with address"),
    QUERY_JOINED_FROM_COUNTRY("", "have joined from country"),
    QUERY_ARE_PLUGIN_GROUP("", "are in ${plugin}'s ${group} Groups"),
    QUERY_OF_PLAYERS("", "of Players who "),
    QUERY_AND("", "and "),
    QUERY_PLAYED_BETWEEN("", "Played between"),
    QUERY_REGISTERED_BETWEEN("", "Registered between"),
    QUERY_ZERO_RESULTS("", "Query produced 0 results"),
    QUERY_RESULTS("", "Query Results"),
    QUERY_RESULTS_MATCH("", "matched ${resultCount} players"),
    QUERY_VIEW("", "  View:"),
    QUERY_ACTIVITY_OF_MATCHED_PLAYERS("", "Activity of matched players"),
    QUERY_ACTIVITY_ON("", "Activity on <span id=\"activity-date\"></span>"),
    QUERY_ARE("", "`are`"),
    QUERY_SESSIONS_WITHIN_VIEW("", "Sessions within view"),

    FILTER_GROUP("", "Group: "),
    FILTER_ALL_PLAYERS("", "All players"),
    FILTER_ACTIVITY_INDEX_NOW("", "Current activity group"),
    FILTER_BANNED("", "Ban status"),
    FILTER_OPS("", "Operator status"),
    ;

    private final String key;
    private final String defaultValue;

    JSLang(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getIdentifier() {
        return "HTML - " + name();
    }

    @Override
    public String getKey() { return key; }

    @Override
    public String getDefault() {
        return defaultValue;
    }
}