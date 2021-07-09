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
 * Html String generator for /login and /register page.
 *
 * @author AuroraLS3
 */
public class ErrorsPage implements Page {

    private final String template;
    private final Locale locale;
    private final Theme theme;
    private final VersionChecker versionChecker;

    ErrorsPage(
            String htmlTemplate,
            Locale locale,
            Theme theme,
            VersionChecker versionChecker) {
        this.template = htmlTemplate;
        this.locale = locale;
        this.theme = theme;
        this.versionChecker = versionChecker;
    }

    @Override
    public String toHtml() {
        PlaceholderReplacer placeholders = new PlaceholderReplacer();
        placeholders.put("title", Icon.called("bug").build().toHtml() + " Error logs");
        placeholders.put("titleText", "Error logs");
        placeholders.put("paragraph", buildBody());
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

    private String buildBody() {
        //language=HTML
        return "<table class=\"table\" id=\"tableAccordion\" style='table-layout: fixed;'>\n" +
                "    <thead>\n" +
                "    <tr>\n" +
                "        <th><i class=\"fa fa-fw fa-bug\"></i> Logfile</th>\n" +
                "        <th><i class=\"far fa-fw fa-clock\"></i> Occurrences</th>\n" +
                "    </tr>\n" +
                "    </thead>\n" +
                "    <tbody id=\"error-logs\"></tbody>\n" +
                "</table>\n" +
                "<script src=\"./js/xmlhttprequests.js\"></script>\n" +
                "<script src=\"./js/loadErrors.js\"></script>";
    }

}