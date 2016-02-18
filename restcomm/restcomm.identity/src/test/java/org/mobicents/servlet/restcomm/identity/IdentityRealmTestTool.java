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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.apache.commons.lang.NotImplementedException;

import junit.framework.Assert;

/**
 * Subclass of IdentityRealmTool that holds fixed values for integration
 * testing with keycloak inside restcomm.keycloak maven project.
 *
 * @author Orestis Tsakiridis
 */
public class IdentityRealmTestTool extends IdentityRealmTool {
    public static String AUTH_SERVER_BASE_URL = "http://127.0.0.1:8081";
    static String MASTER_ADMIN_USERNAME = "admin"; // administrator user for the master realm
    static String MASTER_ADMIN_PASSWORD = "admin";
    static String KEYCLOAK_CLIENT_ID = "keycloak-test"; // this keycloak client is used initialy to get a token for admin
    static String KEYCLOAK_CLIENT_SECRET = "c2e4d5df-47a0-49fb-b321-8d8b0eb81351"; // this is the default secret

    public IdentityRealmTestTool(String authServerBaseUrl, String adminUsername, String adminPassword, String keycloakClientId, String keycloakClientSecret) {
        super(authServerBaseUrl, adminUsername, adminPassword, keycloakClientId, keycloakClientSecret);
    }

    public IdentityRealmTestTool() {
        super(AUTH_SERVER_BASE_URL, MASTER_ADMIN_USERNAME, MASTER_ADMIN_PASSWORD, KEYCLOAK_CLIENT_ID, KEYCLOAK_CLIENT_SECRET);
    }

    /**
     * Check if a role is inside the mappings.
     *
     * @param element
     * @param role
     * @param type This should be "realm" or "client".
     * @return
     */
    public static boolean roleMappingsContainRole(JsonElement element, String role, String instanceId, String type) {
        if ("client".equals(type)) {
            JsonElement mappingsElement = element.getAsJsonObject().get("clientMappings").getAsJsonObject().get(instanceId + "-restcomm-rest").getAsJsonObject().get("mappings").getAsJsonArray();
            Assert.assertNotNull(mappingsElement);
            JsonArray mappingsArray = mappingsElement.getAsJsonArray();
            Assert.assertTrue(mappingsArray.size() > 0);
            boolean developerRoleFound = false;
            for (int i = 0; i < mappingsArray.size(); i++) {
                if (role.equals(mappingsArray.get(i).getAsJsonObject().get("name").getAsString()))
                    return true;
            }
            return false;
        }
        throw new NotImplementedException();
    }
}
