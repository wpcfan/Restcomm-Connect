package org.mobicents.servlet.restcomm.rvd;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.archive.ShrinkWrapMaven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * @author orestis.tsakiridis@telestax.com - Orestis Tsakiridis
 */
@RunWith(Arquillian.class)
public class WebTriggerTest extends RvdTest {

    private final static Logger logger = Logger.getLogger(SettingsRestServiceTest.class);
    private static final String version = org.mobicents.servlet.restcomm.Version.getVersion();

    private static String workspaceLocation;

    @Test
    public void webTriggerNoForking() {
        String createdCallSid = "CA597f980a90fa4f078ec4cb72af81d1d8";
        stubFor(post(urlMatching("/restcomm/2012-04-24/Accounts/ACae6e420f425248d6a26948c17a9e2acf/Calls.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "    \"sid\": \"CA597f980a90fa4f078ec4cb72af81d1d8\",\n" +
                                "        \"InstanceId\": \"IDdce3b0b7646a460c83e1be2a1c079f33\",\n" +
                                "        \"account_sid\": \"ACae6e420f425248d6a26948c17a9e2acf\",\n" +
                                "        \"to\": \"alice\",\n" +
                                "        \"from\": \"AP3eb80c8b\",\n" +
                                "        \"caller_name\": \"AP3eb80c8b\"\n" +
                                "}")));

        // check html method
        Client jersey = getClient(username, password);
        WebResource resource = jersey.resource( getResourceUrl("/services/apps/AP3eb80c8b448f4573aa78ee39ec761b90/start?token=token") );
        ClientResponse response = resource.get(ClientResponse.class);
        Assert.assertEquals(200, response.getStatus());
        String responseBody = response.getEntity(String.class);
        Assert.assertTrue("Newlly created call SID is not part of WebTrigger response", responseBody.contains("["+createdCallSid+"]"));
        //check json method
        resource = jersey.resource( getResourceUrl("/services/apps/AP3eb80c8b448f4573aa78ee39ec761b90/start.json?token=token") );
        response = resource.get(ClientResponse.class);
        Assert.assertEquals(200, response.getStatus());
        String json = response.getEntity(String.class);
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(json).getAsJsonObject();
        Assert.assertEquals("Expected 'success'", object.get("status").getAsString(),"success");
        Assert.assertEquals("Info for a single should be returned", object.get("data").getAsJsonArray().size(),1);
        JsonObject callObject = object.get("data").getAsJsonArray().get(0).getAsJsonObject();
        Assert.assertEquals("Invalid call sid returned",callObject.get("sid").getAsString(),"CA597f980a90fa4f078ec4cb72af81d1d8");
    }

    @Test
    public void webTriggerYesForking() {
        String createdCallSid = "CA597f980a90fa4f078ec4cb72af81d1d8";
        String createdCallSid2 = "CA22222222222222222222222222222222";
        stubFor(post(urlMatching("/restcomm/2012-04-24/Accounts/ACae6e420f425248d6a26948c17a9e2acf/Calls.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\n" +
                                "  \"sid\": \"CA597f980a90fa4f078ec4cb72af81d1d8\",\n" +
                                "  \"InstanceId\": \"IDdce3b0b7646a460c83e1be2a1c079f33\",\n" +
                                "  \"account_sid\": \"ACae6e420f425248d6a26948c17a9e2acf\",\n" +
                                "  \"to\": \"alice\",\n" +
                                "  \"from\": \"AP3eb80c8b\",\n" +
                                "  \"caller_name\": \"AP3eb80c8b\"\n" +
                                "},\n" +
                                "{\n" +
                                "  \"sid\": \"CA22222222222222222222222222222222\",\n" +
                                "  \"InstanceId\": \"IDdce3b0b7646a460c83e1be2a1c079f33\",\n" +
                                "  \"account_sid\": \"ACae6e420f425248d6a26948c17a9e2acf\",\n" +
                                "  \"to\": \"alice\",\n" +
                                "  \"from\": \"AP3eb80c8b\",\n" +
                                "  \"caller_name\": \"AP3eb80c8b\"\n" +
                                "}]")));

        // check html method
        Client jersey = getClient(username, password);
        WebResource resource = jersey.resource( getResourceUrl("/services/apps/AP3eb80c8b448f4573aa78ee39ec761b90/start?token=token") );
        ClientResponse response = resource.get(ClientResponse.class);
        Assert.assertEquals(200, response.getStatus());
        String responseBody = response.getEntity(String.class);
        Assert.assertTrue("Newlly created call SID is not part of WebTrigger response", responseBody.contains("["+createdCallSid+","+createdCallSid2+"]"));
        //check json method
        resource = jersey.resource( getResourceUrl("/services/apps/AP3eb80c8b448f4573aa78ee39ec761b90/start.json?token=token") );
        response = resource.get(ClientResponse.class);
        Assert.assertEquals(200, response.getStatus());
        String json = response.getEntity(String.class);
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(json).getAsJsonObject();
        Assert.assertEquals("Expected 'success'", object.get("status").getAsString(),"success");
        Assert.assertEquals("Info for a single should be returned", object.get("data").getAsJsonArray().size(),2);
        JsonObject callObject = object.get("data").getAsJsonArray().get(0).getAsJsonObject();
        Assert.assertEquals("Invalid call sid returned",callObject.get("sid").getAsString(),"CA597f980a90fa4f078ec4cb72af81d1d8");
        callObject = object.get("data").getAsJsonArray().get(1).getAsJsonObject();
        Assert.assertEquals("Invalid call sid returned",callObject.get("sid").getAsString(),"CA22222222222222222222222222222222");

    }

    @Test
    public void unauthorizedThrown() {
        // TODO
    }

    @Test
    public void basicHttpAuthWorks() {
        // TODO
    }

    @Test
    public void basicHttpAuthReturns401() {
        // TODO
    }

    @Deployment(name = "WebTriggerTest", managed = true, testable = false)
    public static WebArchive createWebArchive() throws IOException {
        logger.info("Packaging Test App");
        logger.info("version");
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "restcomm-rvd.war");
        final WebArchive restcommArchive = ShrinkWrapMaven.resolver()
                .resolve("com.telestax.servlet:restcomm-rvd:war:" + version).withoutTransitivity()
                .asSingle(WebArchive.class);
        archive = archive.merge(restcommArchive);

        archive.addAsWebInfResource("restcomm.xml", "restcomm.xml");

        // create a temporary workspace
        File sourceWorkspace = new File(ProjectRestServiceTest.class.getResource("/workspace_WebTriggerTest").getFile());
        File clonedWorkspace = cloneWorkspace(sourceWorkspace);
        System.out.println(clonedWorkspace);
        workspaceLocation = clonedWorkspace.getPath();
        // replace rvd.xml in WebArcive
        archive.delete("/WEB-INF/rvd.xml");
        String rvdxmlUpdated = customizeRvdXMLForWorkspaceLocation("/rvd.xml",workspaceLocation);
        StringAsset rvdxmlAsset = new StringAsset(rvdxmlUpdated);
        archive.addAsWebInfResource(rvdxmlAsset, "rvd.xml");
        System.out.println("rvd.xml (effective): " + rvdxmlUpdated);

        logger.info("Packaged Test App");
        return archive;
    }
}
