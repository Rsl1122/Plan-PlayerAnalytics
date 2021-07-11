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
 * {@link Lang} implementation for in depth help language when /command ? is used.
 *
 * @author AuroraLS3
 */
public enum DeepHelpLang implements Lang {
    SERVER("", "In Depth Help - /plan server", "Obtain a link to the /server page of a specific server, or the current server if no arguments are given."),
    SERVERS("", "In Depth Help - /plan servers", "List ids, names and uuids of servers in the database."),
    NETWORK("", "In Depth Help - /plan network", "Obtain a link to the /network page, only does so on networks."),
    PLAYER("", "In Depth Help - /plan player", "Obtain a link to the /player page of a specific player, or the current player."),
    PLAYERS("", "In Depth Help - /plan players", "Obtain a link to the /players page to see a list of players."),
    SEARCH("", "In Depth Help - /plan search", "List all matching player names to given part of a name."),
    INGAME("", "In Depth Help - /plan ingame", "Displays some information about the player in game."),
    REGISTER("", "In Depth Help - /plan register", "Use without arguments to get link to register page. Use --code [code] after registration to get a user."),
    UNREGISTER("", "In Depth Help - /plan unregister", "Use without arguments to unregister player linked user, or with username argument to unregister another user."),
    LOGOUT("", "In Depth Help - /plan logout", "Give username argument to log out another user from the panel, give * as argument to log out everyone."),
    INFO("", "In Depth Help - /plan info", "Display the current status of the plugin."),
    RELOAD("", "In Depth Help - /plan reload", "Disable and enable the plugin to reload any changes in config."),
    DISABLE("", "In Depth Help - /plan disable", "Disable the plugin or part of it until next reload/restart."),
    USERS("", "In Depth Help - /plan users", "Lists web users as a table."),
    DB("", "In Depth Help - /plan db", "Use different database subcommands to change the data in some way"),
    DB_BACKUP("", "In Depth Help - /plan db backup", "Uses SQLite to backup the target database to a file."),
    DB_RESTORE("", "In Depth Help - /plan db restore", "Uses SQLite backup file and overwrites contents of the target database."),
    DB_MOVE("", "In Depth Help - /plan db move", "Overwrites contents in the other database with the contents in another."),
    DB_HOTSWAP("", "In Depth Help - /plan db hotswap", "Reloads the plugin with the other database and changes the config to match."),
    DB_CLEAR("", "In Depth Help - /plan db clear", "Clears all Plan tables, removing all Plan-data in the process."),
    DB_REMOVE("", "In Depth Help - /plan db remove", "Removes all data linked to a player from the Current database."),
    DB_UNINSTALLED("", "In Depth Help - /plan db uninstalled", "Marks a server in Plan database as uninstalled so that it will not show up in server queries."),
    EXPORT("", "In Depth Help - /plan export", "Performs an export to export location defined in the config."),
    IMPORT("", "In Depth Help - /plan import", "Performs an import to load data into the database."),
    JSON("", "In Depth Help - /plan json", "Allows you to download a player's data in json format. All of it.");

    private final String key;
    private final String identifier;
    private final String defaultValue;

    DeepHelpLang(String key, String identifier, String defaultValue) {
        this.key = key;
        this.identifier = identifier;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getKey() { return key; }

    @Override
    public String getDefault() {
        return defaultValue;
    }

}
