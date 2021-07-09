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

import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.formatting.PlaceholderReplacer;
import com.djrapitops.plan.delivery.rendering.html.Contributors;
import com.djrapitops.plan.delivery.rendering.html.Html;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.java.UnaryChain;
import com.djrapitops.plan.version.VersionChecker;

import java.util.UUID;

/**
 * Html String generator for /player page.
 *
 * @author AuroraLS3
 */
public class PlayerPage implements Page {

    private final String templateHtml;
    private final PlayerContainer player;

    private final VersionChecker versionChecker;

    private final PlanConfig config;
    private final PageFactory pageFactory;
    private final Theme theme;
    private final Locale locale;
    private final ServerInfo serverInfo;

    private final Formatter<Long> clockLongFormatter;
    private final Formatter<Long> secondLongFormatter;

    PlayerPage(
            String templateHtml,
            PlayerContainer player,
            VersionChecker versionChecker,
            PlanConfig config,
            PageFactory pageFactory,
            Theme theme,
            Locale locale,
            Formatters formatters,
            ServerInfo serverInfo
    ) {
        this.templateHtml = templateHtml;
        this.player = player;
        this.versionChecker = versionChecker;
        this.config = config;
        this.pageFactory = pageFactory;
        this.theme = theme;
        this.locale = locale;
        this.serverInfo = serverInfo;

        clockLongFormatter = formatters.clockLong();
        secondLongFormatter = formatters.secondLong();
    }

    @Override
    public String toHtml() {
        if (!player.getValue(PlayerKeys.REGISTERED).isPresent()) {
            throw new IllegalStateException("Player is not registered");
        }
        return createFor(player);
    }

    public String createFor(PlayerContainer player) {
        long now = System.currentTimeMillis();
        UUID playerUUID = player.getUnsafe(PlayerKeys.UUID);

        PlaceholderReplacer placeholders = new PlaceholderReplacer();

        placeholders.put("refresh", clockLongFormatter.apply(now));
        placeholders.put("refreshFull", secondLongFormatter.apply(now));
        placeholders.put("versionButton", versionChecker.getUpdateButton().orElse(versionChecker.getCurrentVersionButton()));
        placeholders.put("version", versionChecker.getCurrentVersion());
        placeholders.put("updateModal", versionChecker.getUpdateModal());

        String playerName = player.getValue(PlayerKeys.NAME).orElse(playerUUID.toString());
        placeholders.put("playerName", playerName);

        placeholders.put("timeZone", config.getTimeZoneOffsetHours());
        placeholders.put("gmPieColors", theme.getValue(ThemeVal.GRAPH_GM_PIE));

        placeholders.put("contributors", Contributors.generateContributorHtml());

        PlaceholderReplacer pluginPlaceholders = new PlaceholderReplacer();
        PlayerPluginTab pluginTabs = pageFactory.inspectPluginTabs(playerUUID);

        pluginPlaceholders.put("playerName", playerName);
        pluginPlaceholders.put("backButton", (serverInfo.getServer().isProxy() ? Html.BACK_BUTTON_NETWORK : Html.BACK_BUTTON_SERVER).create());
        pluginPlaceholders.put("navPluginsTabs", pluginTabs.getNav());
        pluginPlaceholders.put("pluginsTabs", pluginTabs.getTab());

        return UnaryChain.of(templateHtml)
                .chain(theme::replaceThemeColors)
                .chain(placeholders::apply)
                .chain(pluginPlaceholders::apply)
                .chain(locale::replaceLanguageInHtml)
                .apply();
    }
}
