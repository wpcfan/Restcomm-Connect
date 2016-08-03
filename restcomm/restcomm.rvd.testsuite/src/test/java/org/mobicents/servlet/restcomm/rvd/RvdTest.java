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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;

import java.io.File;
import java.net.URL;
import java.util.Collection;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * @author orestis.tsakiridis@telestax.com - Orestis Tsakiridis
 */
public class RvdTest {

    @ArquillianResource
    URL deploymentUrl;

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

}
