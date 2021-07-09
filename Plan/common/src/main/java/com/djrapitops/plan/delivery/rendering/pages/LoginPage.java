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

import com.djrapitops.plan.delivery.formatting.PlaceholderReplacer;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.utilities.java.UnaryChain;

import com.djrapitops.plan.version.VersionChecker;

/**
 * Html String generator for /login and /register page.
 *
 * @author AuroraLS3
 */
public class LoginPage implements Page {

    private final String template;
    private final ServerInfo serverInfo;
    private final Locale locale;
    private final Theme theme;

    private final VersionChecker versionChecker;

    LoginPage(
            String htmlTemplate,
            ServerInfo serverInfo,
            Locale locale,
            Theme theme,
            VersionChecker versionChecker
    ) {
        this.template = htmlTemplate;
        this.serverInfo = serverInfo;
        this.locale = locale;
        this.theme = theme;
        this.versionChecker = versionChecker;
    }

    @Override
    public String toHtml() {
        PlaceholderReplacer placeholders = new PlaceholderReplacer();
        placeholders.put("command", getCommand());
        placeholders.put("version", versionChecker.getCurrentVersion());
        return UnaryChain.of(template)
                .chain(theme::replaceThemeColors)
                .chain(placeholders::apply)
                .chain(locale::replaceLanguageInHtml)
                .apply();
    }

    private String getCommand() {
        if (serverInfo.getServer().isProxy()) return "planproxy";
        return "plan";
    }
}