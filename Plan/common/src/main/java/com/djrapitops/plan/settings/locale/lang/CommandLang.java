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
 * {@link Lang} implementation for general command language.
 *
 * @author AuroraLS3
 */
public enum CommandLang implements Lang {
    CONFIRM_EXPIRED("", "Cmd Confirm - Expired", "Confirmation expired, use the command again"),
    CONFIRM_FAIL_ACCEPT("", "Cmd Confirm - Fail on accept", "The accepted action errored upon execution: ${0}"),
    CONFIRM_FAIL_DENY("", "Cmd Confirm - Fail on deny", "The denied action errored upon execution: ${0}"),
    CONFIRM("", "Cmd Confirm - confirmation", "Confirm: "),
    CONFIRM_ACCEPT("", "Cmd Confirm - accept", "Accept"),
    CONFIRM_DENY("", "Cmd Confirm - deny", "Cancel"),
    CONFIRM_OVERWRITE_DB("", "Cmd Confirm - overwriting db", "You are about to overwrite data in Plan ${0} with data in ${1}"),
    CONFIRM_CLEAR_DB("", "Cmd Confirm - clearing db", "You are about to remove all Plan-data in ${0}"),
    CONFIRM_REMOVE_PLAYER_DB("", "Cmd Confirm - remove player db", "You are about to remove data of ${0} from ${1}"),
    CONFIRM_UNREGISTER("", "Cmd Confirm - unregister", "You are about to unregister '${0}' linked to ${1}"),
    CONFIRM_CANCELLED_DATA("", "Cmd Confirm - cancelled, no data change", "Cancelled. No data was changed."),
    CONFIRM_CANCELLED_UNREGISTER("", "Cmd Confirm - cancelled, unregister", "Cancelled. '${0}' was not unregistered"),

    FAIL_PLAYER_NOT_FOUND("", "Cmd FAIL - No player", "Player '${0}' was not found, they have no UUID."),
    FAIL_PLAYER_NOT_FOUND_REGISTER("", "Cmd FAIL - No player register", "Player '${0}' was not found in the database."),
    FAIL_SERVER_NOT_FOUND("", "Cmd FAIL - No server", "Server '${0}' was not found from the database."),
    FAIL_EMPTY_SEARCH_STRING("", "Cmd FAIL - Empty search string", "The search string can not be empty"),
    FAIL_ACCEPTS_ARGUMENTS("", "Cmd FAIL - Accepts only these arguments", "Accepts following as ${0}: ${1}"),
    FAIL_REQ_ARGS("", "Cmd FAIL - Requires Arguments", "§cArguments required (${0}) ${1}"),
    FAIL_REQ_ONE_ARG("", "Cmd FAIL - Require only one Argument", "§cSingle Argument required ${1}"),
    FAIL_NO_PERMISSION("", "Cmd FAIL - No Permission", "§cYou do not have the required permission."),
    FAIL_USERNAME_NOT_VALID("", "Cmd FAIL - Invalid Username", "§cUser does not have an UUID."),
    FAIL_USERNAME_NOT_KNOWN("", "Cmd FAIL - Unknown Username", "§cUser has not been seen on this server"),
    FAIL_DATABASE_NOT_OPEN("", "Cmd FAIL - Database not open", "§cDatabase is ${0} - Please try again a bit later."),
    WARN_DATABASE_NOT_OPEN("", "Cmd WARN - Database not open", "§eDatabase is ${0} - This might take longer than expected.."),
    USER_NOT_LINKED("", "Cmd FAIL - Users not linked", "User is not linked to your account and you don't have permission to remove other user's accounts."),

    FAIL_WEB_USER_EXISTS("", "Cmd FAIL - WebUser exists", "§cUser already exists!"),
    FAIL_WEB_USER_NOT_EXISTS("", "Cmd FAIL - WebUser does not exists", "§cUser does not exists!"),
    FAIL_NO_SUCH_FEATURE("", "Cmd FAIL - No Feature", "§eDefine a feature to disable! (currently supports ${0})"),
    FAIL_SEE_CONFIG_SETTING("", "Cmd FAIL - see config", "see '${0}' in config.yml"),

    FEATURE_DISABLED("", "Cmd SUCCESS - Feature disabled", "§aDisabled '${0}' temporarily until next plugin reload."),

    WEB_USER_REGISTER_SUCCESS("", "Cmd SUCCESS - WebUser register", "§aAdded a new user (${0}) successfully!"),
    WEB_USER_REGISTER_NOTIFY("", "Cmd Notify - WebUser register", "Registered new user: '${0}' Perm level: ${1}"),
    WEB_USER_LIST("", "Web User Listing", "  §2${0} §7: §f${1}"),
    NO_WEB_USER_NOTIFY("", "Cmd Notify - No WebUser", "You might not have a web user, use /plan register <password>"),
    WEB_PERMISSION_LEVELS("", "Cmd Web - Permission Levels", ">\\§70: Access all pages\\§71: Access '/players' and all player pages\\§72: Access player page with the same username as the webuser\\§73+: No permissions"),

    LINK_CLICK_ME("", "Cmd - Click Me", "Click me"),
    LINK("", "Cmd - Link", "Link"),
    LINK_SERVER("", "Cmd - Link Server", "Server page: "),
    LINK_PLAYER("", "Cmd - Link Player", "Player page: "),
    LINK_PLAYERS("", "Cmd - Link Players", "Players page: "),
    LINK_NETWORK("", "Cmd - Link Network", "Network page: "),
    LINK_JSON("", "Cmd - Link Player JSON", "Player json: "),
    LINK_REGISTER("", "Cmd - Link Register", "Register page: "),

    HEADER_HELP("", "Cmd Header - Help", "> §2/${0} Help"),
    FOOTER_HELP("", "Cmd Footer - Help", "§7Hover over command or arguments or use '/${0} ?' to learn more about them."),
    HEADER_SEARCH("", "Cmd Header - Search", "> §2${0} Results for §f${1}§2:"),
    HEADER_ANALYSIS("", "Cmd Header - Analysis", "> §2Analysis Results"),
    HEADER_INFO("", "Cmd Header - Info", "> §2Player Analytics"),
    HEADER_INSPECT("", "Cmd Header - Inspect", "> §2Player: §f${0}"),
    HEADER_SERVERS("", "Cmd Header - Servers", "> §2Servers"),
    HEADER_PLAYERS("", "Cmd Header - Players", "> §2Players"),
    HEADER_WEB_USERS("", "Cmd Header - Web Users", "> §2${0} Web Users"),
    HEADER_NETWORK("", "Cmd Header - Network", "> §2Network Page"),
    HEADER_SERVER_LIST("", "Cmd Header - server list", "id::name::uuid"),
    HEADER_WEB_USER_LIST("", "Cmd Header - web user list", "username::linked to::permission level"),

    INFO_VERSION("", "Cmd Info - Version", "  §2Version: §f${0}"),
    INFO_UPDATE("", "Cmd Info - Update", "  §2Update Available: §f${0}"),
    INFO_DATABASE("", "Cmd Info - Database", "  §2Current Database: §f${0}"),
    INFO_PROXY_CONNECTION("", "Cmd Info - Bungee Connection", "  §2Connected to Proxy: §f${0}"),

    INGAME_ACTIVITY_INDEX("", "Cmd Qinspect - Activity Index", "  §2Activity Index: §f${0} | ${1}"),
    INGAME_REGISTERED("", "Cmd Qinspect - Registered", "  §2Registered: §f${0}"),
    INGAME_LAST_SEEN("", "Cmd Qinspect - Last Seen", "  §2Last Seen: §f${0}"),
    INGAME_GEOLOCATION("", "Cmd Qinspect - Geolocation", "  §2Logged in from: §f${0}"),
    INGAME_PLAYTIME("", "Cmd Qinspect - Playtime", "  §2Playtime: §f${0}"),
    INGAME_ACTIVE_PLAYTIME("", "Cmd Qinspect - Active Playtime", "  §2Active Playtime: §f${0}"),
    INGAME_AFK_PLAYTIME("", "Cmd Qinspect - AFK Playtime", "  §2AFK Time: §f${0}"),
    INGAME_LONGEST_SESSION("", "Cmd Qinspect - Longest Session", "  §2Longest Session: §f${0}"),
    INGAME_TIMES_KICKED("", "Cmd Qinspect - Times Kicked", "  §2Times Kicked: §f${0}"),
    INGAME_PLAYER_KILLS("", "Cmd Qinspect - Player Kills", "  §2Player Kills: §f${0}"),
    INGAME_MOB_KILLS("", "Cmd Qinspect - Mob Kills", "  §2Mob Kills: §f${0}"),
    INGAME_DEATHS("", "Cmd Qinspect - Deaths", "  §2Deaths: §f${0}"),

    DB_BACKUP_CREATE("", "Cmd db - creating backup", "Creating a backup file '${0}.db' with contents of ${1}"),
    DB_WRITE("", "Cmd db - write", "Writing to ${0}.."),
    DB_REMOVAL("", "Cmd db - removal", "Removing Plan-data from ${0}.."),
    DB_REMOVAL_PLAYER("", "Cmd db - removal player", "Removing data of ${0} from ${1}.."),
    DB_UNINSTALLED("", "Cmd db - server uninstalled", "§aIf the server is still installed, it will automatically set itself as installed in the database."),
    UNREGISTER("", "Cmd unregister - unregistering", "Unregistering '${0}'.."),

    DISABLE_DISABLED("", "Cmd Disable - Disabled", "§aPlan systems are now disabled. You can still use reload to restart the plugin."),

    NOTIFY_NO_NETWORK("", "Cmd network - No network", "Server is not connected to a network. The link redirects to server page."),
    RELOAD_COMPLETE("", "Cmd Info - Reload Complete", "§aReload Complete"),
    RELOAD_FAILED("", "Cmd Info - Reload Failed", "§cSomething went wrong during reload of the plugin, a restart is recommended."),
    NO_ADDRESS_NOTIFY("", "Cmd Notify - No Address", "§eNo address was available - using localhost as fallback. Set up 'Alternative_IP' settings."),
    HOTSWAP_REMINDER("", "Manage - Remind HotSwap", "§eRemember to swap to the new database (/plan db hotswap ${0}) & reload the plugin."),
    PROGRESS_START("", "Manage - Start", "> §2Processing data.."),
    PROGRESS("", "Manage - Progress", "${0} / ${1} processed.."),
    PROGRESS_SUCCESS("", "Manage - Success", "> §aSuccess!"),
    PROGRESS_FAIL("", "Manage - Fail", "> §cSomething went wrong: ${0}"),
    CONFIRMATION("", "Manage - Fail, Confirmation", "> §cAdd '-a' argument to confirm execution: ${0}"),
    IMPORTERS("", "Manage - List Importers", "Importers: "),
    CONFIRM_OVERWRITE("", "Manage - Confirm Overwrite", "Data in ${0} will be overwritten!"),
    CONFIRM_REMOVAL("", "Manage - Confirm Removal", "Data in ${0} will be removed!"),
    FAIL_SAME_DB("", "Manage - Fail Same Database", "> §cCan not operate on to and from the same database!"),
    FAIL_INCORRECT_DB("", "Manage - Fail Incorrect Database", "> §c'${0}' is not a supported database."),
    FAIL_FILE_NOT_FOUND("", "Manage - Fail File not found", "> §cNo File found at ${0}"),
    FAIL_IMPORTER_NOT_FOUND("", "Manage - Fail No Importer", "§eImporter '${0}' doesn't exist"),
    FAIL_EXPORTER_NOT_FOUND("", "Manage - Fail No Exporter", "§eExporter '${0}' doesn't exist"),
    NO_SERVER("", "Manage - Fail No Server", "No server found with given parameters."),
    UNINSTALLING_SAME_SERVER("", "Manage - Fail Same server", "Can not mark this server as uninstalled (You are on it)");

    private final String key;
    private final String identifier;
    private final String defaultValue;

    CommandLang(String key, String identifier, String defaultValue) {
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