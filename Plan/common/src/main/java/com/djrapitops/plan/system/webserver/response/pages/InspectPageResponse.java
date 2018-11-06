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
package com.djrapitops.plan.system.webserver.response.pages;

import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.pages.parts.InspectPagePluginsContent;
import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class InspectPageResponse extends PageResponse {

    private final UUID uuid;

    public InspectPageResponse(UUID uuid, String html) {
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(html);
        this.uuid = uuid;
    }

    @Override
    public String getContent() {
        Map<String, String> replaceMap = new HashMap<>();
        InspectPagePluginsContent pluginsTab = (InspectPagePluginsContent)
                ResponseCache.loadResponse(PageId.PLAYER_PLUGINS_TAB.of(uuid));
        String[] inspectPagePluginsTab = pluginsTab != null ? pluginsTab.getContents() : getCalculating();
        replaceMap.put("navPluginsTabs", inspectPagePluginsTab[0]);
        replaceMap.put("pluginsTabs", inspectPagePluginsTab[1]);

        return StringSubstitutor.replace(super.getContent(), replaceMap);
    }

    private String[] getCalculating() {
        return new String[]{"<li><i class=\"fa fa-spin fa-refresh\"></i><a> Calculating...</a></li>", ""};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InspectPageResponse)) return false;
        if (!super.equals(o)) return false;
        InspectPageResponse that = (InspectPageResponse) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uuid);
    }
}
