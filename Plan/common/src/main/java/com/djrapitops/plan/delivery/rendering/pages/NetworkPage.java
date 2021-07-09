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
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.delivery.webserver.cache.JSONStorage;
import com.djrapitops.plan.extension.implementation.results.ExtensionData;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionServerDataQuery;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.ProxySettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.settings.theme.ThemeVal;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.utilities.java.UnaryChain;
import com.djrapitops.plan.version.VersionChecker;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Html String generator for /network page.
 *
 * @author AuroraLS3
 */
public class NetworkPage implements Page {

    private final String templateHtml;
    private final DBSystem dbSystem;

    private final VersionChecker versionChecker;
    private final PlanConfig config;
    private final Theme theme;
    private final Locale locale;
    private final ServerInfo serverInfo;
    private final JSONStorage jsonStorage;
    private final Formatters formatters;

    NetworkPage(
            String templateHtml,

            DBSystem dbSystem,
            VersionChecker versionChecker,
            PlanConfig config,
            Theme theme,
            Locale locale,
            ServerInfo serverInfo,
            JSONStorage jsonStorage,
            Formatters formatters
    ) {
        this.templateHtml = templateHtml;
        this.dbSystem = dbSystem;
        this.versionChecker = versionChecker;
        this.config = config;
        this.theme = theme;
        this.locale = locale;
        this.serverInfo = serverInfo;
        this.jsonStorage = jsonStorage;
        this.formatters = formatters;
    }

    @Override
    public String toHtml() {
        PlaceholderReplacer placeholders = new PlaceholderReplacer();

        ServerUUID serverUUID = serverInfo.getServerUUID();
        placeholders.put("networkDisplayName", config.get(ProxySettings.NETWORK_NAME));
        placeholders.put("serverName", config.get(ProxySettings.NETWORK_NAME));
        placeholders.put("serverUUID", serverUUID.toString());
        placeholders.put("refreshBarrier", config.get(WebserverSettings.REDUCED_REFRESH_BARRIER));

        placeholders.put("gmPieColors", theme.getValue(ThemeVal.GRAPH_GM_PIE));
        placeholders.put("playersGraphColor", theme.getValue(ThemeVal.GRAPH_PLAYERS_ONLINE));
        placeholders.put("worldMapColLow", theme.getValue(ThemeVal.WORLD_MAP_LOW));
        placeholders.put("worldMapColHigh", theme.getValue(ThemeVal.WORLD_MAP_HIGH));
        placeholders.put("maxPingColor", theme.getValue(ThemeVal.GRAPH_MAX_PING));
        placeholders.put("minPingColor", theme.getValue(ThemeVal.GRAPH_MIN_PING));
        placeholders.put("avgPingColor", theme.getValue(ThemeVal.GRAPH_AVG_PING));
        placeholders.put("timeZone", config.getTimeZoneOffsetHours());
        
        placeholders.put("versionButton", versionChecker.getUpdateButton().orElse(versionChecker.getCurrentVersionButton()));
        placeholders.put("version", versionChecker.getCurrentVersion());
        placeholders.put("updateModal", versionChecker.getUpdateModal());
        placeholders.put("contributors", Contributors.generateContributorHtml());

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
        pluginPlaceholders.put("networkDisplayName", config.get(ProxySettings.NETWORK_NAME));
        pluginPlaceholders.put("serverName", config.get(ProxySettings.NETWORK_NAME));
        pluginPlaceholders.put("serverUUID", serverUUID.toString());
        pluginPlaceholders.put("navPluginsTabs", nav);
        pluginPlaceholders.put("tabsPlugins", StringUtils.remove(tabs, "${backButton}"));

        return UnaryChain.of(templateHtml)
                .chain(theme::replaceThemeColors)
                .chain(placeholders::apply)
                .chain(pluginPlaceholders::apply)
                .chain(locale::replaceLanguageInHtml)
                .apply();
    }
}