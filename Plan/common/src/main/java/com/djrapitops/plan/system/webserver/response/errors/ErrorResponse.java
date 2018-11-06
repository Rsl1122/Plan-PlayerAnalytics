/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.webserver.response.errors;

import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plan.system.webserver.response.pages.PageResponse;
import org.apache.commons.text.StringSubstitutor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents generic HTTP Error response that has the page style in it.
 *
 * @author Rsl1122
 */
public class ErrorResponse extends PageResponse {

    private String title;
    private String paragraph;

    private VersionCheckSystem versionCheckSystem;

    public ErrorResponse(VersionCheckSystem versionCheckSystem, PlanFiles files) throws IOException {
        this.versionCheckSystem = versionCheckSystem;
        setContent(files.readCustomizableResourceFlat("web/error.html"));
    }

    public ErrorResponse(String message) {
        setContent(message);
    }

    public void replacePlaceholders() {
        Map<String, String> placeHolders = new HashMap<>();
        placeHolders.put("title", title);
        String[] split = title.split(">", 3);
        placeHolders.put("titleText", split.length == 3 ? split[2] : title);
        placeHolders.put("paragraph", paragraph);
        placeHolders.put("version", versionCheckSystem.getCurrentVersion());
        placeHolders.put("update", versionCheckSystem.getUpdateHtml().orElse(""));

        setContent(StringSubstitutor.replace(getContent(), placeHolders));
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setParagraph(String paragraph) {
        this.paragraph = paragraph;
    }
}
