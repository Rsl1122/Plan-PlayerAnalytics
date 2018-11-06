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
package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.system.info.connection.InfoRequestPageHandler;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.pages.*;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.ResponseFactory;
import com.djrapitops.plan.system.webserver.response.errors.BadRequestResponse;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Handles choosing of the correct response to a request.
 *
 * @author Rsl1122
 */
@Singleton
public class ResponseHandler extends TreePageHandler {

    private final DebugPageHandler debugPageHandler;
    private final PlayersPageHandler playersPageHandler;
    private final PlayerPageHandler playerPageHandler;
    private final ServerPageHandler serverPageHandler;
    private final InfoRequestPageHandler infoRequestPageHandler;
    private final ErrorHandler errorHandler;

    private Lazy<WebServer> webServer;

    @Inject
    public ResponseHandler(
            ResponseFactory responseFactory,
            Lazy<WebServer> webServer,

            DebugPageHandler debugPageHandler,
            PlayersPageHandler playersPageHandler,
            PlayerPageHandler playerPageHandler,
            ServerPageHandler serverPageHandler,
            InfoRequestPageHandler infoRequestPageHandler,

            ErrorHandler errorHandler
    ) {
        super(responseFactory);
        this.webServer = webServer;
        this.debugPageHandler = debugPageHandler;
        this.playersPageHandler = playersPageHandler;
        this.playerPageHandler = playerPageHandler;
        this.serverPageHandler = serverPageHandler;
        this.infoRequestPageHandler = infoRequestPageHandler;
        this.errorHandler = errorHandler;
    }

    public void registerPages() {
        registerPage("debug", debugPageHandler);
        registerPage("players", playersPageHandler);
        registerPage("player", playerPageHandler);

        registerPage("network", serverPageHandler);
        registerPage("server", serverPageHandler);

        if (webServer.get().isAuthRequired()) {
            registerPage("", new RootPageHandler(responseFactory));
        } else {
            registerPage("", responseFactory.redirectResponse("/server"), 5);
        }

        registerPage("info", infoRequestPageHandler);
    }

    public Response getResponse(Request request) {
        String targetString = request.getTarget();
        List<String> target = new ArrayList<>(Arrays.asList(targetString.split("/")));
        if (!target.isEmpty()) {
            target.remove(0);
        }
        try {
            return getResponse(request, targetString, target);
        } catch (NoServersException | NotFoundException e) {
            return responseFactory.notFound404(e.getMessage());
        } catch (WebUserAuthException e) {
            return responseFactory.basicAuthFail(e);
        } catch (ForbiddenException e) {
            return responseFactory.forbidden403(e.getMessage());
        } catch (BadRequestException e) {
            return new BadRequestResponse(e.getMessage());
        } catch (UnauthorizedServerException e) {
            return responseFactory.unauthorizedServer(e.getMessage());
        } catch (GatewayException e) {
            return responseFactory.gatewayError504(e.getMessage());
        } catch (InternalErrorException e) {
            if (e.getCause() != null) {
                return responseFactory.internalErrorResponse(e.getCause(), request.getTarget());
            } else {
                return responseFactory.internalErrorResponse(e, request.getTarget());
            }
        } catch (Exception e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
            return responseFactory.internalErrorResponse(e, request.getTarget());
        }
    }

    private Response getResponse(Request request, String targetString, List<String> target) throws WebException {
        Optional<Authentication> authentication = Optional.empty();

        if (targetString.endsWith(".css")) {
            return ResponseCache.loadResponse(PageId.CSS.of(targetString), () -> responseFactory.cssResponse(targetString));
        }
        if (targetString.endsWith(".js")) {
            return ResponseCache.loadResponse(PageId.JS.of(targetString), () -> responseFactory.javaScriptResponse(targetString));
        }
        if (targetString.endsWith("favicon.ico")) {
            return ResponseCache.loadResponse(PageId.FAVICON.id(), responseFactory::faviconResponse);
        }
        boolean isNotInfoRequest = target.isEmpty() || !target.get(0).equals("info");
        boolean isAuthRequired = webServer.get().isAuthRequired() && isNotInfoRequest;
        if (isAuthRequired) {
            authentication = request.getAuth();
            if (!authentication.isPresent()) {
                if (webServer.get().isUsingHTTPS()) {
                    return responseFactory.basicAuth();
                } else {
                    return responseFactory.forbidden403();
                }
            }
        }
        PageHandler pageHandler = getPageHandler(target);
        if (pageHandler == null) {
            return responseFactory.pageNotFound404();
        } else {
            boolean isAuthorized = authentication.isPresent() && pageHandler.isAuthorized(authentication.get(), target);
            if (!isAuthRequired || isAuthorized) {
                return pageHandler.getResponse(request, target);
            }
            return responseFactory.forbidden403();
        }
    }
}
