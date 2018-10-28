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
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.info.request.InfoRequestWithVariables;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.utilities.Verify;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an outbound action request to another Plan server.
 *
 * @author Rsl1122
 */
public class ConnectionOut {

    private final Server toServer;
    private final UUID serverUUID;
    private final InfoRequest infoRequest;

    static {
        try {
            Properties properties = System.getProperties();
            properties.setProperty("sun.net.client.defaultConnectTimeout", Long.toString(TimeUnit.MINUTES.toMillis(1L)));
            properties.setProperty("sun.net.client.defaultReadTimeout", Long.toString(TimeUnit.MINUTES.toMillis(1L)));
            properties.setProperty("sun.net.http.retryPost", Boolean.toString(false));
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "[Plan] Failed to set sun client timeout system properties.", e);
        }
    }

    private final ConnectionLog connectionLog;

    /**
     * Constructor.
     *  @param toServer    Full address to another Plan webserver. (http://something:port)
     * @param serverUUID  UUID of server this outbound connection.
     * @param infoRequest Type of the action this connection wants to be performed.
     * @param connectionLog
     */
    public ConnectionOut(
            Server toServer, UUID serverUUID, InfoRequest infoRequest,
            ConnectionLog connectionLog
    ) {
        this.connectionLog = connectionLog;
        Verify.nullCheck(toServer, serverUUID, infoRequest);
        this.toServer = toServer;
        this.serverUUID = serverUUID;
        this.infoRequest = infoRequest;
    }

    public void sendRequest() throws WebException {
        String address = getAddress();

        CloseableHttpClient client = null;
        HttpPost post = null;
        CloseableHttpResponse response = null;
        try {
            client = getHttpClient(address);
            String url = address + "/info/" + infoRequest.getClass().getSimpleName().toLowerCase();

            post = new HttpPost(url);
            String parameters = parseVariables();
            prepareRequest(post, parameters);

            // Send request
            response = client.execute(post);
            int responseCode = response.getStatusLine().getStatusCode();

            handleResult(url, parameters, responseCode);
        } catch (SocketTimeoutException e) {
            connectionLog.logConnectionTo(toServer, infoRequest, 0);
            throw new ConnectionFailException("Connection to " + address + " timed out after 10 seconds.", e);
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException | IOException e) {
            connectionLog.logConnectionTo(toServer, infoRequest, -1);
            throw new ConnectionFailException("Connection failed to address: " + address + " - Make sure the server is online.", e);
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
            MiscUtils.close(response);
            MiscUtils.close(client);
        }
    }

    private void handleResult(String url, String parameters, int responseCode) throws WebException {
        connectionLog.logConnectionTo(toServer, infoRequest, responseCode);
        switch (responseCode) {
            case 200:
                return;
            case 400:
                throw new BadRequestException("Bad Request: " + url + " | " + parameters);
            case 403:
                throw new ForbiddenException(url + " returned 403 | " + parameters);
            case 404:
                throw new NotFoundException(url + " returned a 404, ensure that your server is connected to an up to date Plan server.");
            case 412:
                throw new UnauthorizedServerException(url + " reported that it does not recognize this server. Make sure '/plan m setup' was successful.");
            case 500:
                throw new InternalErrorException();
            case 504:
                throw new GatewayException(url + " reported that it failed to connect to this server.");
            default:
                throw new WebException(url + "| Wrong response code " + responseCode);
        }
    }

    private void prepareRequest(HttpPost post, String parameters) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(5000)
                .setSocketTimeout(10000)
                .setConnectTimeout(9000)
                .setRedirectsEnabled(true)
                .setRelativeRedirectsAllowed(true)
                .setContentCompressionEnabled(true)
                .build();
        post.setConfig(requestConfig);

        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setHeader("charset", "UTF-8");
        post.setHeader("Connection", "close");

        byte[] toSend = parameters.getBytes();
        post.setEntity(new ByteArrayEntity(toSend));
    }

    private CloseableHttpClient getHttpClient(String address) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        if (address.startsWith("https")) {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustAllStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
            return HttpClients.custom().setSSLSocketFactory(sslsf).build();
        } else {
            return HttpClients.createDefault();
        }
    }

    private String getAddress() {
        String address = toServer.getWebAddress();
        if (address.contains("://:")) {
            String[] parts = address.split("://:", 2);
            address = parts[0] + "://localhost:" + parts[1];
        }
        return address;
    }

    private String parseVariables() {
        StringBuilder parameters = new StringBuilder("sender=" + serverUUID + ";&variable;" +
                "type=" + infoRequest.getClass().getSimpleName());

        if (infoRequest instanceof InfoRequestWithVariables) {
            Map<String, String> variables = ((InfoRequestWithVariables) infoRequest).getVariables();
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                parameters.append(";&variable;").append(entry.getKey()).append("=").append(entry.getValue());
            }
        }

        return parameters.toString();
    }
}
