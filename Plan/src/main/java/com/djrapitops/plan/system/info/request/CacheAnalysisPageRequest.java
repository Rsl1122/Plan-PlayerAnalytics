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
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.DefaultResponses;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.pages.AnalysisPageResponse;
import com.djrapitops.plan.utilities.Base64Util;
import com.djrapitops.plan.utilities.file.export.HtmlExport;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Map;
import java.util.UUID;

/**
 * InfoRequest used to place HTML of a server to ResponseCache.
 *
 * @author Rsl1122
 */
public class CacheAnalysisPageRequest extends InfoRequestWithVariables implements CacheRequest {

    private final PlanConfig config;
    private final Processing processing;
    private final HtmlExport htmlExport;

    private final UUID networkUUID;

    private UUID serverUUID;
    private String html;

    CacheAnalysisPageRequest(
            PlanConfig config,
            Processing processing,
            HtmlExport htmlExport,
            UUID networkUUID
    ) {
        this.config = config;
        this.processing = processing;
        this.networkUUID = networkUUID;
        this.htmlExport = htmlExport;
    }

    CacheAnalysisPageRequest(
            UUID serverUUID, String html,
            PlanConfig config,
            Processing processing,
            HtmlExport htmlExport,
            UUID networkUUID
    ) {
        this.config = config;
        this.processing = processing;
        this.networkUUID = networkUUID;
        this.htmlExport = htmlExport;

        Verify.nullCheck(serverUUID, html);
        this.serverUUID = serverUUID;
        variables.put("html", Base64Util.encode(html));
        this.html = html;
    }

    @Override
    public Response handleRequest(Map<String, String> variables) throws WebException {
        // Available variables: sender, html (Base64)

        UUID sender = UUID.fromString(variables.get("sender"));

        String sentHtml = variables.get("html");
        Verify.nullCheck(sentHtml, () -> new BadRequestException("HTML 'html' variable not supplied in the request"));

        cache(sender, Base64Util.decode(sentHtml));
        return DefaultResponses.SUCCESS.get();
    }

    private void cache(UUID serverUUID, String html) {
        ResponseCache.cacheResponse(PageId.SERVER.of(serverUUID), () -> new AnalysisPageResponse(html));
        if (!networkUUID.equals(serverUUID)) {
            ResponseCache.clearResponse(PageId.SERVER.of(networkUUID));
        }

        if (config.isTrue(Settings.ANALYSIS_EXPORT)) {
            processing.submitNonCritical(() -> htmlExport.exportServer(serverUUID));
        }
    }

    @Override
    public void runLocally() {
        cache(serverUUID, html);
    }
}
