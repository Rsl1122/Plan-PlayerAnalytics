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
import com.djrapitops.plan.delivery.rendering.html.Contributors;
import com.djrapitops.plan.delivery.rendering.html.icon.Icon;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.utilities.java.UnaryChain;
import com.djrapitops.plan.version.VersionChecker;

/**
 * Page to display error stacktrace.
 *
 * @author AuroraLS3
 */
public class ErrorMessagePage implements Page {

    private final String template;
    private final Icon icon;
    private final String errorTitle;
    private final String errorMsg;

    private final Locale locale;
    private final Theme theme;
    private final VersionChecker versionChecker;

    public ErrorMessagePage(
            String template, Icon icon, String errorTitle, String errorMsg,
            Locale locale, Theme theme, VersionChecker versionChecker
    ) {
        this.template = template;
        this.icon = icon;
        this.errorTitle = errorTitle;
        this.errorMsg = errorMsg;
        this.locale = locale;
        this.theme = theme;
        this.versionChecker = versionChecker;
    }

    public ErrorMessagePage(
            String template, String errorTitle, String errorMsg,
            VersionChecker versionChecker,
            Locale locale,
            Theme theme) {
        this(template, Icon.called("exclamation-circle").build(), errorTitle, errorMsg, locale, theme, versionChecker);
    }

    @Override
    public String toHtml() {
        PlaceholderReplacer placeholders = new PlaceholderReplacer();
        placeholders.put("title", icon.toHtml() + " " + errorTitle);
        placeholders.put("titleText", errorTitle);
        placeholders.put("paragraph", errorMsg);
        placeholders.put("versionButton", versionChecker.getUpdateButton().orElse(versionChecker.getCurrentVersionButton()));
        placeholders.put("version", versionChecker.getCurrentVersion());
        placeholders.put("updateModal", versionChecker.getUpdateModal());
        placeholders.put("contributors", Contributors.generateContributorHtml());
        return UnaryChain.of(template)
                .chain(theme::replaceThemeColors)
                .chain(placeholders::apply)
                .chain(locale::replaceLanguageInHtml)
                .apply();
    }
}