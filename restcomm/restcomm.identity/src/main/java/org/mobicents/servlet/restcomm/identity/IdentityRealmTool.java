/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package org.mobicents.servlet.restcomm.identity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class that handles setup/teardown of a keycloak instance.
 *
 * It's used both in restcomm.identity and restcomm.testsuite maven projects
 * for testing.
 *
 * Requirements:
 *      - admin account in master realm
 *      - existence of an app/client with Direct Grants enabled
 *
 * @author Orestis Tsakiridis
 */
public class IdentityRealmTool {
    private String token;

    private String authServerBaseUrl;
    private String adminUsername;
    private String adminPassword;
    private String keycloakClientId;
    private String keycloakClientSecret;

    public IdentityRealmTool(String authServerBaseUrl, String adminUsername, String adminPassword, String keycloakClientId, String keycloakClientSecret) {
        this.authServerBaseUrl = authServerBaseUrl;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.keycloakClientId = keycloakClientId;
        this.keycloakClientSecret = keycloakClientSecret;
    }

    public String getToken() {
        if (this.token == null) {
            Client jerseyClient = Client.create();
            jerseyClient.addFilter(new HTTPBasicAuthFilter(keycloakClientId, keycloakClientSecret));
            WebResource webResource = jerseyClient.resource(authServerBaseUrl + "/auth/realms/master/protocol/openid-connect/token");
            MultivaluedMap params = new MultivaluedMapImpl();
            params.add("username", adminUsername);
            params.add("password", adminPassword);
            params.add("grant_type", "password");
            params.add("client_id", keycloakClientId);
            String response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).accept(MediaType.APPLICATION_JSON).post(String.class, params);
            JsonParser parser = new JsonParser();
            JsonObject jsonResponse = parser.parse(response).getAsJsonObject();
            String token = jsonResponse.getAsJsonPrimitive("access_token").getAsString();
            //System.out.println("Using identity token: " + token);

            this.token = token;
        }
        return this.token;
    }

    public void importRealm(String resourceFileName) throws IOException {
        String token = getToken();

        InputStream inputStream = getClass().getResourceAsStream(resourceFileName);
        String data = IOUtils.toString(inputStream,"UTF-8");

        Client jerseyClient = Client.create();
        WebResource webResource = jerseyClient.resource(authServerBaseUrl + "/auth/admin/realms");
        String response = webResource.type(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "Bearer " + token).post(String.class, data);
    }

    public void dropRealm(String realmName) {
        String token = getToken();

        Client jerseyClient = Client.create();
        WebResource webResource = jerseyClient.resource(authServerBaseUrl + "/auth/admin/realms/restcomm");
        webResource.header("Authorization", "Bearer " + token).delete();
    }
}
