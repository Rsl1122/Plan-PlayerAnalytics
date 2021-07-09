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
package com.djrapitops.plan.delivery.rendering.pages;

import com.djrapitops.plan.delivery.domain.container.CachingSupplier;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.formatting.PlaceholderReplacer;
import com.djrapitops.plan.delivery.rendering.html.Contributors;
import com.djrapitops.plan.delivery.rendering.html.Html;
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.delivery.webserver.cache.JSONStorage;
import com.djrapitops.plan.extension.implementation.results.ExtensionData;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionServerDataQuery;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.settings.theme.ThemeVal;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.utilities.java.UnaryChain;
import com.djrapitops.plan.version.VersionChecker;

import java.util.List;

/**
 * Html String generator for /server page.
 *
 * @author AuroraLS3
 */
public class ServerPage implements Page {

    private final String templateHtml;
    private final Server server;
    private final PlanConfig config;
    private final Theme theme;
    private final Locale locale;
    private final VersionChecker versionChecker;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final JSONStorage jsonStorage;
    private final Formatters formatters;

    ServerPage(
            String templateHtml, Server server,
            PlanConfig config,
            Theme theme,
            Locale locale,
            VersionChecker versionChecker,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            JSONStorage jsonStorage,
            Formatters formatters
    ) {
        this.templateHtml = templateHtml;
        this.server = server;
        this.config = config;
        this.theme = theme;
        this.locale = locale;
        this.versionChecker = versionChecker;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.jsonStorage = jsonStorage;
        this.formatters = formatters;
    }

    @Override
    public String toHtml() {
        PlaceholderReplacer placeholders = new PlaceholderReplacer();

        ServerUUID serverUUID = server.getUuid();
        placeholders.put("serverUUID", serverUUID.toString());
        placeholders.put("serverName", server.getIdentifiableName());
        placeholders.put("serverDisplayName", server.getName());
        placeholders.put("refreshBarrier", config.get(WebserverSettings.REDUCED_REFRESH_BARRIER));

        placeholders.put("timeZone", config.getTimeZoneOffsetHours());
        placeholders.put("gmPieColors", theme.getValue(ThemeVal.GRAPH_GM_PIE));

        placeholders.put("contributors", Contributors.generateContributorHtml());
        placeholders.put("versionButton", versionChecker.getUpdateButton().orElse(versionChecker.getCurrentVersionButton()));
        placeholders.put("version", versionChecker.getCurrentVersion());
        placeholders.put("updateModal", versionChecker.getUpdateModal());

        CachingSupplier<ServerPluginTabs> pluginTabs = new CachingSupplier<>(() -> {
            List<ExtensionData> extensionData = dbSystem.getDatabase().query(new ExtensionServerDataQuery(serverUUID));
            return new ServerPluginTabs(extensionData, formatters);
        });

        long after = System.currentTimeMillis() - config.get(WebserverSettings.REDUCED_REFRESH_BARRIER);
        String navIdentifier = DataID.EXTENSION_NAV.of(serverUUID);
        String tabIdentifier = DataID.EXTENSION_TABS.of(serverUUID);
        String nav = jsonStorage.fetchJsonMadeAfter(navIdentifier, after).orElseGet(() -> {
            jsonStorage.invalidateOlder(navIdentifier, after);
            return jsonStorage.storeJson(navIdentifier, pluginTabs.get().getNav());
        }).json;
        String tabs = jsonStorage.fetchJsonMadeAfter(tabIdentifier, after).orElseGet(() -> {
            jsonStorage.invalidateOlder(tabIdentifier, after);
            return jsonStorage.storeJson(tabIdentifier, pluginTabs.get().getTabs());
        }).json;

        PlaceholderReplacer pluginPlaceholders = new PlaceholderReplacer();
        pluginPlaceholders.put("serverUUID", serverUUID.toString());
        pluginPlaceholders.put("serverName", server.getIdentifiableName());
        pluginPlaceholders.put("serverDisplayName", server.getName());
        pluginPlaceholders.put("backButton", serverInfo.getServer().isProxy() ? Html.BACK_BUTTON_NETWORK.create() : "");
        pluginPlaceholders.put("navPluginsTabs", nav);
        pluginPlaceholders.put("tabsPlugins", tabs);

        return UnaryChain.of(templateHtml)
                .chain(theme::replaceThemeColors)
                .chain(placeholders::apply)
                .chain(pluginPlaceholders::apply)
                .chain(locale::replaceLanguageInHtml)
                .apply();
    }
}