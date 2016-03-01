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
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.net.URL;

/**
 * @author Orestis Tsakiridis
 */
@RunWith(Arquillian.class)
public class ApplicationsEndpointOauthTest {
    private final static Logger logger = Logger.getLogger(ApplicationsEndpointOauthTest.class);
    private static final String version = org.mobicents.servlet.restcomm.Version.getVersion();

    @ArquillianResource
    private Deployer deployer;
    @ArquillianResource
    URL deploymentUrl;

    private String applicationSid = "AP73926e7113fa4d95981aa96b76eca854";
    private String removedApplicationSid = "APa854f830c928d029a903w0f029a929b9";
    private String accountSid = "ACae6e420f425248d6a26948c17a9e2acf";

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
    public void applicationRetrievalAccessRules() {
        String token = new RestcommIdentityApi(IdentityRealmTestTool.AUTH_SERVER_BASE_URL, "administrator@company.com", "RestComm", "restcomm", null).getTokenString();
        String baseUrl = fixDeploymentUrl();

        Client jerseyClient = Client.create();
        // retrieve application list
        ClientResponse response = jerseyClient.resource(baseUrl + "/2012-04-24/Accounts/" + "ACae6e420f425248d6a26948c17a9e2acf" + "/Applications.json/" ).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get(ClientResponse.class);
        Assert.assertEquals("FAILED: retrieve applications list as JSON", 200, response.getStatus());
        // retrieve single application as JSON
        response = jerseyClient.resource(baseUrl + "/2012-04-24/Accounts/" + "ACae6e420f425248d6a26948c17a9e2acf" + "/Applications/" + applicationSid + ".json").header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get(ClientResponse.class);
        Assert.assertEquals("FAILED: retrieve single application as JSON", 200, response.getStatus());
    }

    @Test
    public void applicationRemovalAccessRules() {
        String token = new RestcommIdentityApi(IdentityRealmTestTool.AUTH_SERVER_BASE_URL, "administrator@company.com", "RestComm", "restcomm", null).getTokenString();
        String baseUrl = fixDeploymentUrl();

        Client jerseyClient = Client.create();
        // remove application
        ClientResponse response = jerseyClient.resource(baseUrl + "/2012-04-24/Accounts/" + "ACae6e420f425248d6a26948c17a9e2acf" + "/Applications/" + removedApplicationSid + ".json/" ).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).delete(ClientResponse.class);
        Assert.assertEquals("FAILED: remove application", 200, response.getStatus());
    }

    @Test
    public void createApplicationAccessRules() {
        String token = new RestcommIdentityApi(IdentityRealmTestTool.AUTH_SERVER_BASE_URL, "administrator@company.com", "RestComm", "restcomm", null).getTokenString();
        String baseUrl = fixDeploymentUrl();

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("FriendlyName", "New application");
        params.add("VoiceCallerIdLookup", "true");
        params.add("RcmlUrl", "/restcomm/rcmlurl/test");
        params.add("Kind", "voice");

        Client jerseyClient = Client.create();
        // create application
        ClientResponse response = jerseyClient.resource(baseUrl + "/2012-04-24/Accounts/" + "ACae6e420f425248d6a26948c17a9e2acf" + "/Applications.json" ).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).post(ClientResponse.class,params);
        Assert.assertEquals("FAILED: create application", 200, response.getStatus());
    }

    @Test
    public void updateApplicationAccessRules() {
        String token = new RestcommIdentityApi(IdentityRealmTestTool.AUTH_SERVER_BASE_URL, "administrator@company.com", "RestComm", "restcomm", null).getTokenString();
        String baseUrl = fixDeploymentUrl();

        Client jerseyClient = Client.create();
        // create application
        ClientResponse response = jerseyClient.resource(baseUrl + "/2012-04-24/Accounts/" + accountSid + "/Applications.json" ).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).post(ClientResponse.class,params);
        Assert.assertEquals("FAILED: create application", 200, response.getStatus());
    }



    private String fixDeploymentUrl() {
        String deploymentUrl = this.deploymentUrl.toString();
        if (deploymentUrl.endsWith("/")) {
            return deploymentUrl.substring(0, deploymentUrl.length() - 1);
        }
        return deploymentUrl;
    }

    @Deployment(name = "ApplicationsEndpointOauthTest", managed = true, testable = false)
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
