package com.djrapitops.plan.common.system.webserver.response.pages;

import com.djrapitops.plan.common.api.exceptions.connection.ConnectionFailException;
import com.djrapitops.plan.common.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.common.api.exceptions.connection.WebException;
import com.djrapitops.plan.common.system.info.InfoSystem;
import com.djrapitops.plan.common.system.processing.Processing;
import com.djrapitops.plan.common.system.webserver.pages.parsing.AnalysisPage;
import com.djrapitops.plan.common.system.webserver.response.Response;
import com.djrapitops.plan.common.system.webserver.response.cache.PageId;
import com.djrapitops.plan.common.system.webserver.response.cache.ResponseCache;
import com.djrapitops.plan.common.system.webserver.response.errors.NotFoundResponse;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.UUID;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class AnalysisPageResponse extends Response {

    public static AnalysisPageResponse refreshNow(UUID serverUUID) {
        Processing.submitNonCritical(() -> {
            try {
                InfoSystem.getInstance().generateAnalysisPage(serverUUID);
            } catch (NoServersException | ConnectionFailException e) {
                ResponseCache.cacheResponse(PageId.SERVER.of(serverUUID), () -> new NotFoundResponse(e.getMessage()));
            } catch (WebException e) {
                Log.toLog(AnalysisPageResponse.class.getName(), e);
            }
        });
        return new AnalysisPageResponse(AnalysisPage.getRefreshingHtml());
    }

    public AnalysisPageResponse(String html) {
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(html);
    }
}
