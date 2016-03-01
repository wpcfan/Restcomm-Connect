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

package org.mobicents.servlet.restcomm.http.identity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.archive.ShrinkWrapMaven;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mobicents.servlet.restcomm.identity.RestcommIdentityApi;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.net.URL;

/**
 * @author Orestis Tsakiridis
 *
 * TODO Review all tests after https://github.com/RestComm/RestComm-Core/issues/898 is settled.
 */
@RunWith(Arquillian.class)
public class AccountsEndpointOauthTest {
    private final static Logger logger = Logger.getLogger(AccountsEndpointOauthTest.class);
    private static final String version = org.mobicents.servlet.restcomm.Version.getVersion();

    @ArquillianResource
    private Deployer deployer;
    @ArquillianResource
    URL deploymentUrl;

    private String unlinkedAccountSid = "AC39239204948584090289495039384949";
    private String removedAccountSid = "AC95729572957361563840483726484939";
    private String updatedAccountSid = "AC84769385783047164960483859372923";

    private static IdentityRealmTestTool tool;

    @BeforeClass
    public static void setupRealm() throws RestcommIdentityApi.RestcommIdentityApiException, IOException {
        tool = new IdentityRealmTestTool();
        tool.importRealm("realm-with-identity-instance.json");
    }


    @AfterClass
    public static void teardownRealm() throws RestcommIdentityApi.RestcommIdentityApiException {
        tool.dropRealm("restcomm");
    }

    @After
    public void after() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Test
    public void accountRetrievalAccessRules() {
        RestcommIdentityApi api = new RestcommIdentityApi(IdentityRealmTestTool.AUTH_SERVER_BASE_URL, "devuser@company.com", "RestComm", "restcomm", null);
        String token =  api.getTokenString();
        RestcommIdentityApi adminApi = new RestcommIdentityApi(IdentityRealmTestTool.AUTH_SERVER_BASE_URL, "administrator@company.com", "RestComm", "restcomm", null);
        String adminToken =  adminApi.getTokenString();
        String baseUrl = fixDeploymentUrl();

        Client jerseyClient = Client.create();
        // retrieve accounts as JSON
        ClientResponse response = jerseyClient.resource(baseUrl + "/2012-04-24/Accounts.json").header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get(ClientResponse.class);
        Assert.assertEquals("FAILED: retrieve accounts as JSON", 200, response.getStatus());
        // retrieve single account as JSON
        response = jerseyClient.resource(baseUrl + "/2012-04-24/Accounts.json/AC54b41ed43f543e9ca3c8da489b0c1631").header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get(ClientResponse.class);
        Assert.assertEquals("FAILED: retrieve single account as JSON", 200, response.getStatus());
        // retrieval of other single accounts should be prohibited
        response = jerseyClient.resource(baseUrl + "/2012-04-24/Accounts.json/ACae6e420f425248d6a26948c17a9e2acf").header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get(ClientResponse.class);
        Assert.assertEquals("FAILED: retrieval of other single accounts should be prohibited", 401, response.getStatus());
        // retrieval of other single account by admin should be allowed
        response = jerseyClient.resource(baseUrl + "/2012-04-24/Accounts.json/AC54b41ed43f543e9ca3c8da489b0c1631").header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken).get(ClientResponse.class);
        Assert.assertEquals("FAILED: retrieval of other single account by admin should be allowed", 200, response.getStatus());
    }

    @Test
    public void accountCreationAndLinkingAccessRules() {
        RestcommIdentityApi adminApi = new RestcommIdentityApi(IdentityRealmTestTool.AUTH_SERVER_BASE_URL, "administrator@company.com", "RestComm", "restcomm", null);
        String adminToken = adminApi.getTokenString();
        String baseUrl = fixDeploymentUrl();

        Client jerseyClient = Client.create();
        // admin can create sub-accounts
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("Status", "ACTIVE");
        params.add("FriendlyName", "New User");
        ClientResponse response = jerseyClient.resource(baseUrl + "/2012-04-24/Accounts.json").header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken).post(ClientResponse.class, params);
        Assert.assertEquals("FAILED: Administrator user can create sub-accounts", 200, response.getStatus());
        String body = response.getEntity(String.class);
        JsonObject jsonObject = new JsonParser().parse(body).getAsJsonObject();
        String newUserSid = jsonObject.getAsJsonPrimitive("sid").getAsString();
        Assert.assertNotNull(newUserSid);
        // create user and link account to it
        params = new MultivaluedMapImpl();
        params.add("username", "newuser@company.com");
        params.add("password", "RestComm");
        params.add("create", "true");
        params.add("friendly_name", "New User");
        response = jerseyClient.resource(baseUrl + "/2012-04-24/Accounts/" + newUserSid + "/operations/link").header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken).post(ClientResponse.class, params);
        Assert.assertEquals("FAILED: linking 'New User' account with new keycloak user - newuser@company.com", 200, response.getStatus());
        // access newlly created account/user
        RestcommIdentityApi api = new RestcommIdentityApi(IdentityRealmTestTool.AUTH_SERVER_BASE_URL, "newuser@company.com", "RestComm", "restcomm", null);
        String token = adminApi.getTokenString();
        response = jerseyClient.resource(baseUrl + "/2012-04-24/Accounts.json/" + newUserSid).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get(ClientResponse.class);
        Assert.assertEquals("FAILED: access newly created account", 200, response.getStatus());
    }

    @Test
    public void accountRemovalAccessRules() {
        String baseUrl = fixDeploymentUrl();
        String token = new RestcommIdentityApi(IdentityRealmTestTool.AUTH_SERVER_BASE_URL, "devuser@company.com", "RestComm", "restcomm", null).getTokenString();
        String removedUserToken = new RestcommIdentityApi(IdentityRealmTestTool.AUTH_SERVER_BASE_URL, "removed@company.com", "RestComm", "restcomm", null).getTokenString();
        String adminToken = new RestcommIdentityApi(IdentityRealmTestTool.AUTH_SERVER_BASE_URL, "administrator@company.com", "RestComm", "restcomm", null).getTokenString();

        Client jerseyClient = Client.create();
        // a (Developer) user cannot remove accounts linked to other users
        ClientResponse response = jerseyClient.resource(baseUrl + "/2012-04-24/Accounts/" + removedAccountSid).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).delete(ClientResponse.class);
        Assert.assertEquals("FAILED: A user should not be able to remove accounts linked to other users", 401, response.getStatus());
        // a user can remove his own account (?)
        // TODO the policy for self-removing accounts should be defined. So far restcomm prevents accounts remove themselves (400). But what about the case where the respective oauth tokens are used ?
        response = jerseyClient.resource(baseUrl + "/2012-04-24/Accounts/" + removedAccountSid).header(HttpHeaders.AUTHORIZATION, "Bearer " + removedUserToken).delete(ClientResponse.class);
        Assert.assertEquals("FAILED: A user should not be able to remove his own account", 400, response.getStatus());
        // an administrator should be able to remove a user's account
        // TODO administrator can remove accounts only if they are his children. The policy should be defined here (at least for SSO branch).
        response = jerseyClient.resource(baseUrl + "/2012-04-24/Accounts/" + removedAccountSid).header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken).delete(ClientResponse.class);
        Assert.assertEquals("FAILED: Administrator should be able to remove a user's account", 200, response.getStatus());
    }

    @Test
    public void accountUpdateAccessRules() {
        String baseUrl = fixDeploymentUrl();
        String token = new RestcommIdentityApi(IdentityRealmTestTool.AUTH_SERVER_BASE_URL, "updated@company.com", "RestComm", "restcomm", null).getTokenString();
        String adminToken = new RestcommIdentityApi(IdentityRealmTestTool.AUTH_SERVER_BASE_URL, "administrator@company.com", "RestComm", "restcomm", null).getTokenString();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("FriendlyName", "Account is updated");

        Client jerseyClient = Client.create();
        // a (Developer) user can update his account
        ClientResponse response = jerseyClient.resource(baseUrl + "/2012-04-24/Accounts/" + updatedAccountSid + ".json").header(HttpHeaders.AUTHORIZATION, "Bearer " + token).post(ClientResponse.class, params);
        Assert.assertEquals("FAILED: A user should be able to update  his account", 200, response.getStatus());
        // an administrator should be able to remove a user's account
        // TODO administrator can remove accounts only if they are his children. The policy should be defined here (at least for SSO branch).
        response = jerseyClient.resource(baseUrl + "/2012-04-24/Accounts/" + updatedAccountSid).header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken).post(ClientResponse.class, params);
        Assert.assertEquals("FAILED: Administrator should be able to update his children accounts", 200, response.getStatus());
    }

    @Test
    public void accountLinking() {
        RestcommIdentityApi adminApi = new RestcommIdentityApi(IdentityRealmTestTool.AUTH_SERVER_BASE_URL, "administrator@company.com", "RestComm", "restcomm", null);
        String adminToken = adminApi.getTokenString();
        String baseUrl = fixDeploymentUrl();
        Client jerseyClient = Client.create();

        // account access by user unlinked@company.com should fail at first
        RestcommIdentityApi api = new RestcommIdentityApi(IdentityRealmTestTool.AUTH_SERVER_BASE_URL, "unlinked@company.com", "RestComm", "restcomm", null);
        String token = adminApi.getTokenString();
        ClientResponse response = jerseyClient.resource(baseUrl + "/2012-04-24/Accounts.json/" + unlinkedAccountSid).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get(ClientResponse.class);
        Assert.assertEquals("FAILED: user unlinked@company.com should not have access to account", 401, response.getStatus());
        // account-to-user linking by admin
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("username", "unlinked@company.com");
        response = jerseyClient.resource(baseUrl + "/2012-04-24/Accounts/" + unlinkedAccountSid + "/operations/link").header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken).post(ClientResponse.class, params);
        Assert.assertEquals("FAILED: account linking to unlinked@company.com user executed by admin", 200, response.getStatus());
        // access newlly created account/user. Request a new token.
        api = new RestcommIdentityApi(IdentityRealmTestTool.AUTH_SERVER_BASE_URL, "unlinked@company.com", "RestComm", "restcomm", null);
        token = api.getTokenString();
        response = jerseyClient.resource(baseUrl + "/2012-04-24/Accounts.json/" + unlinkedAccountSid).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get(ClientResponse.class);
        Assert.assertEquals("FAILED: user unlinked@company.com should be able to access his account after linking", 200, response.getStatus());

    }



    private String fixDeploymentUrl() {
        String deploymentUrl = this.deploymentUrl.toString();
        if (deploymentUrl.endsWith("/")) {
            return deploymentUrl.substring(0, deploymentUrl.length() - 1);
        }
        return deploymentUrl;
    }


    @Deployment(name = "AccountsEndpointOauthTest", managed = true, testable = false)
    public static WebArchive createWebArchiveNoGw() {
        logger.info("Packaging Test App");
        logger.info("version");
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "restcomm.war");
        final WebArchive restcommArchive = ShrinkWrapMaven.resolver()
                .resolve("com.telestax.servlet:restcomm.application:war:" + version).withoutTransitivity()
                .asSingle(WebArchive.class);
        archive = archive.merge(restcommArchive);
        archive.delete("/WEB-INF/sip.xml");
        archive.delete("/WEB-INF/conf/restcomm.xml");
        archive.delete("/WEB-INF/data/hsql/restcomm.script");
        archive.addAsWebInfResource("sip.xml");
        archive.addAsWebInfResource("restcomm.xml", "conf/restcomm.xml");
        archive.addAsWebInfResource("identity_restcomm.script", "data/hsql/restcomm.script");
        logger.info("Packaged Test App");
        return archive;
    }

}
