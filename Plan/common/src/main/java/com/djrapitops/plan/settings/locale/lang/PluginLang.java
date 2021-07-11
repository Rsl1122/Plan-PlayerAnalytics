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
 * {@link Lang} implementation for Language that is logged when the plugin enables or disables.
 *
 * @author AuroraLS3
 */
public enum PluginLang implements Lang {
    ENABLED("", "Enable", "Player Analytics Enabled."),
    ENABLED_WEB_SERVER("", "Enable - WebServer", "Webserver running on PORT ${0} ( ${1} )"),
    ENABLED_DATABASE("", "Enable - Database", "${0}-database connection established."),
    API_ADD_RESOURCE_JS("", "API - js+", "PageExtension: ${0} added javascript(s) to ${1}, ${2}"),
    API_ADD_RESOURCE_CSS("", "API - css+", "PageExtension: ${0} added stylesheet(s) to ${1}, ${2}"),

    ENABLE_NOTIFY_STORING_PRESERVED_SESSIONS("", "Enable - Storing preserved sessions", "Storing sessions that were preserved before previous shutdown."),
    ENABLE_NOTIFY_EMPTY_IP("", "Enable - Notify Empty IP", "IP in server.properties is empty & Alternative_IP is not in use. Incorrect links might be given!"),
    ENABLE_NOTIFY_BAD_IP("", "Enable - Notify Bad IP", "0.0.0.0 is not a valid address, set up Alternative_IP settings. Incorrect links might be given!"),
    ENABLE_NOTIFY_WEB_SERVER_DISABLED("", "Enable - Notify Webserver disabled", "WebServer was not initialized. (WebServer.DisableWebServer: true)"),
    ENABLE_NOTIFY_GEOLOCATIONS_INTERNET_REQUIRED("", "Enable - Notify Geolocations Internet Required", "Plan Requires internet access on first run to download GeoLite2 Geolocation database."),
    ENABLE_NOTIFY_GEOLOCATIONS_DISABLED("", "Enable - Notify Geolocations disabled", "Geolocation gathering is not active. (Data.Geolocations: false)"),
    ENABLE_FAIL_DB("", "Enable FAIL - Database", "${0}-Database Connection failed: ${1}"),
    ENABLE_FAIL_WRONG_DB("", "Enable FAIL - Wrong Database Type", "${0} is not a supported Database"),
    ENABLE_FAIL_DB_PATCH("", "Enable FAIL - Database Patch", "Database Patching failed, plugin has to be disabled. Please report this issue"),
    ENABLE_FAIL_NO_WEB_SERVER_PROXY("", "Enable FAIL - WebServer (Proxy)", "WebServer did not initialize!"),
    ENABLE_FAIL_GEODB_WRITE("", "Enable FAIL - GeoDB Write", "Something went wrong saving the downloaded GeoLite2 Geolocation database"),

    WEB_SERVER_FAIL_PORT_BIND("", "WebServer FAIL - Port Bind", "WebServer was not initialized successfully. Is the port (${0}) in use?"),
    WEB_SERVER_FAIL_SSL_CONTEXT("", "WebServer FAIL - SSL Context", "WebServer: SSL Context Initialization Failed."),
    WEB_SERVER_FAIL_STORE_LOAD("", "WebServer FAIL - Store Load", "WebServer: SSL Certificate loading Failed."),
    WEB_SERVER_FAIL_EMPTY_FILE("", "WebServer FAIL - EOF", "WebServer: EOF when reading Certificate file. (Check that the file is not empty)"),
    WEB_SERVER_NOTIFY_NO_CERT_FILE("", "WebServer - Notify no Cert file", "WebServer: Certificate KeyStore File not Found: ${0}"),
    WEB_SERVER_NOTIFY_HTTP("", "WebServer - Notify HTTP", "WebServer: No Certificate -> Using HTTP-server for Visualization."),
    WEB_SERVER_NOTIFY_USING_PROXY_MODE("", "WebServer - Notify Using Proxy", "WebServer: Proxy-mode HTTPS enabled, make sure that your reverse-proxy is routing using HTTPS and Plan Alternative_IP.Address points to the Proxy"),
    WEB_SERVER_NOTIFY_HTTP_USER_AUTH("", "WebServer - Notify HTTP User Auth", "WebServer: User Authorization Disabled! (Not secure over HTTP)"),
    WEB_SERVER_NOTIFY_HTTPS_USER_AUTH("", "WebServer - Notify HTTPS User Auth", "WebServer: User Authorization Disabled! (Disabled in config)"),
    WEB_SERVER_NOTIFY_IP_WHITELIST("", "Webserver - Notify IP Whitelist", "Webserver: IP Whitelist is enabled."),
    WEB_SERVER_NOTIFY_IP_WHITELIST_BLOCK("", "Webserver - Notify IP Whitelist Block", "Webserver: ${0} was denied access to '${1}'. (not whitelisted)"),

    DISABLED("", "Disable", "Player Analytics Disabled."),
    DISABLED_WEB_SERVER("", "Disable - WebServer", "Webserver has been disabled."),
    DISABLED_PROCESSING("", "Disable - Processing", "Processing critical unprocessed tasks. (${0})"),
    DISABLED_PROCESSING_COMPLETE("", "Disable - Processing Complete", "Processing complete."),
    DISABLED_UNSAVED_SESSIONS("", "Disable - Unsaved Session Save", "Saving unfinished sessions.."),
    DISABLED_UNSAVED_SESSIONS_TIMEOUT("", "Disable - Unsaved Session Save Timeout", "Timeout hit, storing the unfinished sessions on next enable instead."),
    DISABLED_WAITING_SQLITE("", "Disable - Waiting SQLite", "Waiting queries to finish to avoid SQLite crashing JVM.."),
    DISABLED_WAITING_SQLITE_COMPLETE("", "Disable - Waiting SQLite Complete", "Closed SQLite connection."),
    DISABLED_WAITING_TRANSACTIONS("", "Disable - Waiting Transactions", "Waiting for unfinished transactions to avoid data loss.."),
    DISABLED_WAITING_TRANSACTIONS_COMPLETE("", "Disable - Waiting Transactions Complete", "Transaction queue closed."),

    VERSION_NEWEST("", "Version - Latest", "You're using the latest version."),
    VERSION_AVAILABLE("", "Version - New", "New Release (${0}) is available ${1}"),
    VERSION_AVAILABLE_SPIGOT("", "Version - New (old)", "New Version is available at ${0}"),
    VERSION_AVAILABLE_DEV("", "Version - DEV", " This is a DEV release."),
    VERSION_FAIL_READ_VERSIONS("", "Version FAIL - Read versions.txt", "Version information could not be loaded from Github/versions.txt"),
    VERSION_FAIL_READ_OLD("", "Version FAIL - Read info (old)", "Failed to check newest version number"),

    VERSION_UPDATE("", "HTML - Version Update", "Update"),
    VERSION_UPDATE_AVAILABLE("", "HTML - Version Update Available", "Version ${0} is Available!"),
    VERSION_UPDATE_INFO("", "HTML - Version Update Info", "A new version has been released and is now available for download."),
    VERSION_UPDATE_DEV("", "HTML - Version Update Dev", "This version is a DEV release."),
    VERSION_CHANGE_LOG("", "HTML - Version Change log", "View Changelog"),
    VERSION_DOWNLOAD("", "HTML - Version Download", "Download Plan-${0}.jar"),
    VERSION_CURRENT("", "HTML - Version Current", "You have version ${0}"),

    DB_APPLY_PATCH("", "Database - Apply Patch", "Applying Patch: ${0}.."),
    DB_APPLIED_PATCHES("", "Database - Patches Applied", "All database patches applied successfully."),
    DB_APPLIED_PATCHES_ALREADY("", "Database - Patches Applied Already", "All database patches already applied."),
    DB_NOTIFY_CLEAN("", "Database Notify - Clean", "Removed data of ${0} players."),
    DB_NOTIFY_SQLITE_WAL("", "Database Notify - SQLite No WAL", "SQLite WAL mode not supported on this server version, using default. This may or may not affect performance."),
    DB_MYSQL_LAUNCH_OPTIONS_FAIL("", "Database MySQL - Launch Options Error", "Launch Options were faulty, using default (${0})"),
    LOADING_SERVER_INFO("", "ServerInfo - Loading", "Loading server identifying information");

    private final String key;
    private final String identifier;
    private final String defaultValue;

    PluginLang(String key, String identifier, String defaultValue) {
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