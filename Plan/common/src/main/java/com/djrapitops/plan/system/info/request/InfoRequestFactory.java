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

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.webserver.response.ResponseFactory;
import com.djrapitops.plan.utilities.file.export.HtmlExport;
import com.djrapitops.plan.utilities.html.pages.PageFactory;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.task.RunnableFactory;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

/**
 * Factory for {@link InfoRequest} objects used for server-server communications.
 *
 * @author Rsl1122
 */
@Singleton
public class InfoRequestFactory {

    private final Lazy<PlanPlugin> plugin;
    private final Lazy<PlanConfig> config;
    private final Lazy<Processing> processing;
    private final Lazy<InfoSystem> infoSystem;
    private final Lazy<ConnectionSystem> connectionSystem;
    private final Lazy<ServerInfo> serverInfo;
    private final Lazy<InfoRequestFactory> infoRequestFactory;
    private final Lazy<ResponseFactory> responseFactory;
    private final Lazy<PageFactory> pageFactory;
    private final Lazy<HtmlExport> htmlExport;
    private final Lazy<PluginLogger> logger;
    private final Lazy<RunnableFactory> runnableFactory;

    @Inject
    public InfoRequestFactory(
            Lazy<PlanPlugin> plugin,
            Lazy<PlanConfig> config,
            Lazy<Processing> processing,
            Lazy<InfoSystem> infoSystem,
            Lazy<ConnectionSystem> connectionSystem,
            Lazy<ServerInfo> serverInfo,
            Lazy<InfoRequestFactory> infoRequestFactory,
            Lazy<ResponseFactory> responseFactory,
            Lazy<PageFactory> pageFactory,
            Lazy<HtmlExport> htmlExport,
            Lazy<PluginLogger> logger,
            Lazy<RunnableFactory> runnableFactory
    ) {
        this.plugin = plugin;
        this.config = config;
        this.processing = processing;
        this.infoSystem = infoSystem;
        this.connectionSystem = connectionSystem;
        this.serverInfo = serverInfo;
        this.infoRequestFactory = infoRequestFactory;
        this.responseFactory = responseFactory;
        this.pageFactory = pageFactory;
        this.htmlExport = htmlExport;
        this.logger = logger;
        this.runnableFactory = runnableFactory;
    }

    public CacheRequest cacheAnalysisPageRequest(UUID serverUUID, String html) {
        return new CacheAnalysisPageRequest(serverUUID, html, config.get(), processing.get(), htmlExport.get(), serverInfo.get().getServerUUID());
    }

    public CacheRequest cacheInspectPageRequest(UUID uuid, String html) {
        return new CacheInspectPageRequest(uuid, html, config.get(), processing.get(), serverInfo.get(), htmlExport.get());
    }

    public CacheRequest cacheInspectPluginsTabRequest(UUID uuid, String nav, String html) {
        return new CacheInspectPluginsTabRequest(uuid, nav, html, serverInfo.get());
    }

    public CacheRequest cacheNetworkPageContentRequest(UUID serverUUID, String html) {
        return new CacheNetworkPageContentRequest(serverUUID, html, serverInfo.get());
    }

    public GenerateRequest generateAnalysisPageRequest(UUID serverUUID) {
        return new GenerateAnalysisPageRequest(serverUUID, infoRequestFactory.get(), serverInfo.get(), infoSystem.get(), pageFactory.get());
    }

    public GenerateRequest generateInspectPageRequest(UUID uuid) {
        return new GenerateInspectPageRequest(uuid, this, responseFactory.get(), pageFactory.get(), infoSystem.get());
    }

    public GenerateInspectPluginsTabRequest generateInspectPluginsTabRequest(UUID uuid) {
        return new GenerateInspectPluginsTabRequest(uuid, infoSystem.get(), this, pageFactory.get());
    }

    public SaveDBSettingsRequest saveDBSettingsRequest() {
        return new SaveDBSettingsRequest(plugin.get(), config.get(), logger.get(), runnableFactory.get());
    }

    public SetupRequest sendDBSettingsRequest(String addressOfThisServer) {
        return new SendDBSettingsRequest(addressOfThisServer, config.get(), this, connectionSystem.get());
    }

    public CheckConnectionRequest checkConnectionRequest(String webAddress) {
        return new CheckConnectionRequest(webAddress, connectionSystem.get());
    }
}