/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
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

package org.mobicents.servlet.restcomm.rvd;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.*;
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
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Random;

import static com.github.tomakehurst.wiremock.client.WireMock.*;



/**
 * @author orestis.tsakiridis@telestax.com - Orestis Tsakiridis
 */
@RunWith(Arquillian.class)
public class ProjectRestServiceTest extends RvdTest {

    private final static Logger logger = Logger.getLogger(ProjectRestServiceTest.class);
    private static final String version = org.mobicents.servlet.restcomm.Version.getVersion();

    static final String username = "administrator@company.com";
    static final String password = "adminpass";
    private String accountSid = "ACae6e420f425248d6a26948c17a9e2acf";
    private String accountAuthToken = "77f8c12cc7b8f8423e5c38b035249166";

    private String removedProjectSid = "AP81cf45088cba4abcac1261385916d582";

    private static String workspaceLocation;

    @BeforeClass
    public static void beforeClass() {
        //System.out.println("checking for workspace existence: " + workspaceLocation);
    }

    @AfterClass
    public static void afterClass() throws IOException {
        // remove the workspace directory created with the archive deployment
        if (workspaceLocation != null) {
            FileUtils.deleteDirectory(new File(workspaceLocation));
        }
    }

    @Before
    public void before() {
        //stubFor(get(urlEqualTo("/restcomm/1012-04-24/Accounts.json/"+username))
        stubFor(get(urlMatching("/restcomm/2012-04-24/Accounts.json/administrator@company.com"))
//                .withHeader("Accept", equalTo("text/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"sid\":\"ACae6e420f425248d6a26948c17a9e2acf\",\"email_address\":\"administrator@company.com\",\"status\":\"active\",\"role\":\"administrator\"}")));
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    @Test
    public void canRetrieveProjects() {
        Client jersey = getClient(username, password);
        WebResource resource = jersey.resource( getResourceUrl("/services/projects") );
        ClientResponse response = resource.get(ClientResponse.class);
        Assert.assertEquals(200, response.getStatus());

        String json = response.getEntity(String.class);
        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(json).getAsJsonArray();
        Assert.assertTrue("Invalid number of project returned", array.size() >= 2); // takes into account projects that have been created/deleted meanwhile
    }

    @Test
    public void canCreateProject() {
        // create application stub
        String createApplicationSid = "AP03d28db981ee4aa0888ebebd35b4dd4f";
        stubFor(post(urlMatching("/restcomm/2012-04-24/Accounts/ACae6e420f425248d6a26948c17a9e2acf/Applications.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"sid\":\""+createApplicationSid+"\",\"friendly_name\":\"newapplication\"}")));
        // retrieve project created
        stubFor(get(urlMatching("/restcomm/2012-04-24/Accounts/ACae6e420f425248d6a26948c17a9e2acf/Applications/"+createApplicationSid+".json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"sid\":\""+createApplicationSid+"\",\"friendly_name\":\"newapplication\"}")));
        // update project
        stubFor(post(urlMatching("/restcomm/2012-04-24/Accounts/ACae6e420f425248d6a26948c17a9e2acf/Applications/"+createApplicationSid+".json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"sid\":\""+createApplicationSid+"\",\"friendly_name\":\"newapplication\"}")));

        Client jersey = getClient(username, password);
        WebResource resource = jersey.resource( getResourceUrl("/services/projects/newapplication?kind=voice") );
        ClientResponse response = resource.put(ClientResponse.class);
        Assert.assertEquals(200, response.getStatus());

        String json = response.getEntity(String.class);
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(json).getAsJsonObject();
        Assert.assertEquals("Invalid project friendly name", "newapplication", object.get("name").getAsString());
        Assert.assertEquals("Invalid project sid", createApplicationSid, object.get("sid").getAsString());
        Assert.assertEquals("Invalid project kind", "voice", object.get("kind").getAsString());

        // check directory existence inside workspace
        String createdProjectPath = workspaceLocation + "/" + createApplicationSid;
        Assert.assertTrue("Created project directory does not exist: " + createdProjectPath,  new File(createdProjectPath).exists());
    }

    @Test
    public void canDeleteProjects() {
        stubFor(delete(urlMatching("/restcomm/2012-04-24/Accounts/ACae6e420f425248d6a26948c17a9e2acf/Applications/"+removedProjectSid+".json"))
                .willReturn(aResponse()
                        .withStatus(200)));

        Client jersey = getClient(username, password);
        WebResource resource = jersey.resource( getResourceUrl("/services/projects/" + removedProjectSid) );
        ClientResponse response = resource.delete(ClientResponse.class);
        Assert.assertEquals("Error removing project " + removedProjectSid,200, response.getStatus());

        // check directory has been actually removed
        String removedProjectPath = workspaceLocation + "/" + removedProjectSid;
        Assert.assertFalse("Removed project directory still exists: " + removedProjectPath, new File(removedProjectPath).exists());

    }


    /**
     * Clones (makes a recursive copy of) a workspace from sourceDir into a random temporary
     * directory.
     *
     * @param sourceDir
     * @return the temporary workspace directory created
     */
    public static File cloneWorkspace(File sourceDir) throws IOException {
        String tempDirLocation = System.getProperty("java.io.tmpdir");
        Random ran = new Random();
        String workspaceLocation = tempDirLocation + "/workspace_projectTest" + ran.nextInt(10000);
        File workspaceDir = new File(workspaceLocation);
        FileUtils.copyDirectory(sourceDir,workspaceDir);
        //workspaceDir.mkdir();

        return workspaceDir;
    }

    public static void removeTempWorkspace(String workspaceLocation) {
        File workspaceDir = new File(workspaceLocation);
        FileUtils.deleteQuietly(workspaceDir);
    }

    @Deployment(name = "ProjectRestServiceTest", managed = true, testable = false)
    public static WebArchive createWebArchive() throws IOException {
        logger.info("Packaging Test App");
        logger.info("version");
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "restcomm-rvd.war");
        final WebArchive restcommArchive = ShrinkWrapMaven.resolver()
                .resolve("com.telestax.servlet:restcomm-rvd:war:" + version).withoutTransitivity()
                .asSingle(WebArchive.class);
        archive = archive.merge(restcommArchive);

        // create a temporary workspace
        File sourceWorkspace = new File(ProjectRestServiceTest.class.getResource("/workspace_projectTest").getFile());
        File clonedWorkspace = cloneWorkspace(sourceWorkspace);
        System.out.println(clonedWorkspace);
        workspaceLocation = clonedWorkspace.getPath();

        // build a proper rvd.xml
        String rvdxml = FileUtils.readFileToString(new File(ProjectRestServiceTest.class.getResource("/rvd.xml").getFile()), Charset.forName("UTF8"));
        System.out.println("rvd.xml: " + rvdxml);
        String rvdxmlUpdated = rvdxml.replace("<workspaceLocation>workspace</workspaceLocation>","<workspaceLocation>"+workspaceLocation+"</workspaceLocation>");
        System.out.println("rvd.xml (after): " + rvdxmlUpdated);

        archive.addAsWebInfResource("restcomm.xml", "restcomm.xml");
        archive.delete("/WEB-INF/rvd.xml");
        StringAsset rvdxmlAsset = new StringAsset(rvdxmlUpdated);
        archive.addAsWebInfResource(rvdxmlAsset, "rvd.xml");

        logger.info("Packaged Test App");
        return archive;
    }
}
