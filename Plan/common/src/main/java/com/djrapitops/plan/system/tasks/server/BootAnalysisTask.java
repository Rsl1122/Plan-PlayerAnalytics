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
package com.djrapitops.plan.system.tasks.server;

import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plan.system.info.request.InfoRequestFactory;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plugin.task.AbsRunnable;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BootAnalysisTask extends AbsRunnable {

    private final InfoSystem infoSystem;
    private final InfoRequestFactory infoRequestFactory;
    private final ServerInfo serverInfo;
    private final WebExceptionLogger webExceptionLogger;

    @Inject
    public BootAnalysisTask(
            InfoSystem infoSystem,
            InfoRequestFactory infoRequestFactory,
            ServerInfo serverInfo,
            WebExceptionLogger webExceptionLogger
    ) {
        this.infoSystem = infoSystem;
        this.infoRequestFactory = infoRequestFactory;
        this.serverInfo = serverInfo;
        this.webExceptionLogger = webExceptionLogger;
    }

    @Override
    public void run() {
        try {
            webExceptionLogger.logIfOccurs(this.getClass(), () ->
                    infoSystem.sendRequest(infoRequestFactory.generateAnalysisPageRequest(serverInfo.getServerUUID()))
            );
        } catch (IllegalStateException ignore) {
            /* Plugin was reloading */
        } finally {
            cancel();
        }
    }
}
