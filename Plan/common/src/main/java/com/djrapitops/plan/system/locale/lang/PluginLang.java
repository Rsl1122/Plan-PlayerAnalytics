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
package com.djrapitops.plan.system.locale.lang;

/**
 * {@link Lang} implementation for Language that is logged when the plugin enables or disables.
 *
 * @author Rsl1122
 */
public enum PluginLang implements Lang {
    ENABLED("Enable", "Player Analytics Enabled."),
    ENABLED_WEB_SERVER("Enable - WebServer", "Webserver running on PORT ${0} (${1})"),
    ENABLED_DATABASE("Enable - Database", "${0}-database connection established."),

    ENABLE_NOTIFY_EMPTY_IP("Enable - Notify Empty IP", "IP in server.properties is empty & AlternativeIP is not in use. Incorrect links will be given!"),
    ENABLE_NOTIFY_WEB_SERVER_DISABLED("Enable - Notify Webserver disabled", "WebServer was not initialized. (WebServer.DisableWebServer: true)"),
    ENABLE_NOTIFY_GEOLOCATIONS_INTERNET_REQUIRED("Enable - Notify Geolocations Internet Required", "Plan Requires internet access on first run to download GeoLite2 Geolocation database."),
    ENABLE_NOTIFY_GEOLOCATIONS_DISABLED("Enable - Notify Geolocations disabled", "Geolocation gathering is not active. (Data.Geolocations: false)"),
    ENABLE_NOTIFY_ADDRESS_CONFIRMATION("Enable - Notify Address Confirmation", "Make sure that this address points to THIS Server: ${0}"),

    ENABLE_FAIL_DB("Enable FAIL - Database", "${0}-Database Connection failed: ${1}"),
    ENABLE_FAIL_WRONG_DB("Enable FAIL - Wrong Database Type", "${0} is not a supported Database"),
    ENABLE_FAIL_DB_PATCH("Enable FAIL - Database Patch", "Database Patching failed, plugin has to be disabled. Please report this issue"),
    ENABLE_FAIL_NO_WEB_SERVER_PROXY("Enable FAIL - WebServer (Proxy)", "WebServer did not initialize!"),
    ENABLE_FAIL_GEODB_WRITE("Enable FAIL - GeoDB Write", "Something went wrong saving the downloaded GeoLite2 Geolocation database"),

    WEB_SERVER_FAIL_PORT_BIND("WebServer FAIL - Port Bind", "WebServer was not initialized successfully. Is the port (${0}) in use?"),
    WEB_SERVER_FAIL_SSL_CONTEXT("WebServer FAIL - SSL Context", "WebServer: SSL Context Initialization Failed."),
    WEB_SERVER_FAIL_STORE_LOAD("WebServer FAIL - Store Load", "WebServer: SSL Certificate loading Failed."),
    WEB_SERVER_NOTIFY_NO_CERT_FILE("WebServer - Notify no Cert file", "WebServer: Certificate KeyStore File not Found: ${0}"),
    WEB_SERVER_NOTIFY_HTTP("WebServer - Notify HTTP", "WebServer: No Certificate -> Using HTTP-server for Visualization."),
    WEB_SERVER_NOTIFY_HTTP_USER_AUTH("WebServer - Notify HTTP User Auth", "WebServer: User Authorization Disabled! (Not secure over HTTP)"),

    DISABLED("Disable", "Player Analytics Disabled."),
    DISABLED_WEB_SERVER("Disable - WebServer", "Webserver has been disabled."),
    DISABLED_PROCESSING("Disable - Processing", "Processing critical unprocessed tasks. (${0})"),
    DISABLED_PROCESSING_COMPLETE("Disable - Processing Complete", "Processing complete."),
    DISABLED_UNSAVED_SESSIONS("Disable - Unsaved Session Save", "Saving unfinished sessions.."),

    VERSION_NEWEST("Version - Latest", "You're using the latest version."),
    VERSION_AVAILABLE("Version - New", "New Release (${0}) is available ${1}"),
    VERSION_AVAILABLE_SPIGOT("Version - New (old)", "New Version is available at ${0}"),
    VERSION_AVAILABLE_DEV("Version - DEV", " This is a DEV release."),
    VERSION_FAIL_READ_VERSIONS("Version FAIL - Read versions.txt", "Version information could not be loaded from Github/versions.txt"),
    VERSION_FAIL_READ_OLD("Version FAIL - Read info (old)", "Failed to check newest version number"),

    DB_APPLY_PATCH("Database - Apply Patch", "Applying Patch: ${0}.."),
    DB_APPLIED_PATCHES("Database - Patches Applied", "All database patches applied successfully."),
    DB_APPLIED_PATCHES_ALREADY("Database - Patches Applied Already", "All database patches already applied."),
    DB_NOTIFY_CLEAN("Database Notify - Clean", "Removed data of ${0} players."),
    DB_NOTIFY_SQLITE_WAL("Database Notify - SQLite No WAL", "SQLite WAL mode not supported on this server version, using default. This may or may not affect performance."),
    DB_MYSQL_LAUNCH_OPTIONS_FAIL("Database MySQL - Launch Options Error", "Launch Options were faulty, using default (${0})");

    private final String identifier;
    private final String defaultValue;

    PluginLang(String identifier, String defaultValue) {
        this.identifier = identifier;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getDefault() {
        return defaultValue;
    }
}