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
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Rule;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Random;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * @author orestis.tsakiridis@telestax.com - Orestis Tsakiridis
 */
public class RvdTest {

    @ArquillianResource
    private Deployer deployer;
    @ArquillianResource
    URL deploymentUrl;

    static final String username = "administrator@company.com";
    static final String password = "adminpass";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

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

    protected Client getClient(String username, String password) {
        Client jersey = Client.create();
        jersey.addFilter(new HTTPBasicAuthFilter(username, password));
        return jersey;
    }

    protected String getResourceUrl(String suffix) {
        String urlString = deploymentUrl.toString();
        if ( urlString.endsWith("/") )
            urlString = urlString.substring(0,urlString.length()-1);

        if ( suffix != null && !suffix.isEmpty()) {
            if (!suffix.startsWith("/"))
                suffix = "/" + suffix;
            return urlString + suffix;
        } else
            return urlString;

    }

    /**
     * Imports a directory into a shrinkwrap archive.
     *
     * Use it like this:
     *   File dir = new File(SettingsTest.class.getResource("/workspace_settings").getPath());
     *   importDirToArchive(dir, archive, "workspace");
     *
     * It will create a directory at the root of the archive named "workspace" and will copy
     * there all files under .../workspace_settings directory path
     *
     * @param dir
     * @param archive
     * @param destination
     */
    public static void importDirToArchive(File dir, WebArchive archive, String destination) {
        Collection<File> files = FileUtils.listFiles(dir, new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
        for ( File file: files) {
            String relativePath = file.getPath().substring(dir.getPath().length());
            System.out.println(file.getName() + " - " + relativePath);
            archive.addAsWebResource(file, destination + relativePath);
        }
        System.out.println(archive.toString(true));
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

    /**
     * Applies boilerplate search-n-replace operation to an rvd.xml resource. It will replace <workspace/> element
     * to the one specified in workspaceLocation. It returns the customized rvd.xml as a string.
     *
     * Example:
     *  String newrvdxml = customizeRvdXMLForWorkspaceLocation("/rvd.xml", "/tmp/my-custom-workspace");
     *
     * @param rvdxmlResource
     * @param workspaceLocation
     * @return
     * @throws IOException
     */
    public static String customizeRvdXMLForWorkspaceLocation(String rvdxmlResource, String workspaceLocation ) throws IOException {
        String rvdxml = FileUtils.readFileToString(new File(ProjectRestServiceTest.class.getResource(rvdxmlResource).getFile()), Charset.forName("UTF8"));
        System.out.println("rvd.xml: " + rvdxml);
        return rvdxml.replace("<workspaceLocation>workspace</workspaceLocation>","<workspaceLocation>"+workspaceLocation+"</workspaceLocation>");
    }

}
