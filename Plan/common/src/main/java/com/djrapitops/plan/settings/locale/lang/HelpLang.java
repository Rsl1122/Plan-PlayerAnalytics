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
 * Lang for short help messages in Commands.
 *
 * @author AuroraLS3
 */
public enum HelpLang implements Lang {
    ARG_SERVER("", "CMD Arg Name - server", "server"),
    ARG_NAME_UUID("", "CMD Arg Name - name or uuid", "name/uuid"),
    ARG_CODE("", "CMD Arg Name - code", "${code}"),
    ARG_USERNAME("", "CMD Arg Name - username", "username"),
    ARG_FEATURE("", "CMD Arg Name - feature", "feature"),
    ARG_SUBCOMMAND("", "CMD Arg Name - subcommand", "subcommand"),
    ARG_BACKUP_FILE("", "CMD Arg Name - backup-file", "backup-file"),
    ARG_EXPORT_KIND("", "CMD Arg Name - export kind", "export kind"),
    ARG_IMPORT_KIND("", "CMD Arg Name - import kind", "import kind"),
    DESC_ARG_SERVER_IDENTIFIER("", "CMD Arg - server identifier", "Name, ID or UUID of a server"),
    DESC_ARG_PLAYER_IDENTIFIER("", "CMD Arg - player identifier", "Name or UUID of a player"),
    DESC_ARG_PLAYER_IDENTIFIER_REMOVE("", "CMD Arg - player identifier remove", "Identifier for a player that will be removed from current database."),
    DESC_ARG_CODE("", "CMD Arg - code", "Code used to finalize registration."),
    DESC_ARG_USERNAME("", "CMD Arg - username", "Username of another user. If not specified player linked user is used."),
    DESC_ARG_FEATURE("", "CMD Arg - feature", "Name of the feature to disable: ${0}"),
    DESC_ARG_SUBCOMMAND("", "CMD Arg - subcommand", "Use the command without subcommand to see help."),
    DESC_ARG_BACKUP_FILE("", "CMD Arg - backup-file", "Name of the backup file (case sensitive)"),
    DESC_ARG_DB_BACKUP("", "CMD Arg - db type backup", "Type of the database to backup. Current database is used if not specified."),
    DESC_ARG_DB_RESTORE("", "CMD Arg - db type restore", "Type of the database to restore to. Current database is used if not specified."),
    DESC_ARG_DB_MOVE_FROM("", "CMD Arg - db type move from", "Type of the database to move data from."),
    DESC_ARG_DB_MOVE_TO("", "CMD Arg - db type move to", "Type of the database to move data to. Can not be same as previous."),
    DESC_ARG_DB_HOTSWAP("", "CMD Arg - db type hotswap", "Type of the database to start using."),
    DESC_ARG_DB_REMOVE("", "CMD Arg - db type clear", "Type of the database to remove all data from."),

    SERVER("", "Command Help - /plan server", "View the Server Page"),
    SERVERS("", "Command Help - /plan servers", "List servers in Database"),
    NETWORK("", "Command Help - /plan network", "View the Network Page"),
    PLAYER("", "Command Help - /plan player", "View a Player Page"),
    PLAYERS("", "Command Help - /plan players", "View the Players Page"),
    SEARCH("", "Command Help - /plan search", "Search for a player name"),
    INGAME("", "Command Help - /plan ingame", "View Player info in game"),
    REGISTER("", "Command Help - /plan register", "Register a user for Plan website"),
    UNREGISTER("", "Command Help - /plan unregister", "Unregister a user of Plan website"),
    INFO("", "Command Help - /plan info", "Information about the plugin"),
    RELOAD("", "Command Help - /plan reload", "Restart Plan"),
    DISABLE("", "Command Help - /plan disable", "Disable the plugin or part of it"),
    USERS("", "Command Help - /plan users", "List all web users"),
    DB("", "Command Help - /plan db", "Manage Plan database"),
    DB_BACKUP("", "Command Help - /plan db backup", "Backup data of a database to a file"),
    DB_RESTORE("", "Command Help - /plan db restore", "Restore data from a file to a database"),
    DB_MOVE("", "Command Help - /plan db move", "Move data between databases"),
    DB_HOTSWAP("", "Command Help - /plan db hotswap", "Change Database quickly"),
    DB_CLEAR("", "Command Help - /plan db clear", "Remove ALL Plan data from a database"),
    DB_REMOVE("", "Command Help - /plan db remove", "Remove player's data from Current database"),
    DB_UNINSTALLED("", "Command Help - /plan db uninstalled", "Set a server as uninstalled in the database."),
    EXPORT("", "Command Help - /plan export", "Export html or json files manually"),
    IMPORT("", "Command Help - /plan import", "Import data"),
    JSON("", "Command Help - /plan json", "View json of Player's raw data."),
    LOGOUT("", "Command Help - /plan logout", "Log out other users from the panel.");

    private final String identifier;
    private final String key;
    private final String defaultValue;

    HelpLang(String key, String identifier, String defaultValue) {
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