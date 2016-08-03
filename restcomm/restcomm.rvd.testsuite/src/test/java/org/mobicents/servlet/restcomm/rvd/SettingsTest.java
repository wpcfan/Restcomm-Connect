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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.log4j.Logger;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.archive.ShrinkWrapMaven;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URL;
import java.util.Collection;

/**
 * @author orestis.tsakriridis@telestax.com - Orestis Tsakiridis
 */
@RunWith(Arquillian.class)
public class SettingsTest extends RvdTest {

    private final static Logger logger = Logger.getLogger(SettingsTest.class);
    private static final String version = org.mobicents.servlet.restcomm.Version.getVersion();

    static final String username = "administrator@company.com";
    static final String password = "adminpass";


    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    @Test
    public void checkSettingsLifecycle() {
        Client jersey = getClient(username, password);
        // retrieve initial settings (empty)
        WebResource resource = jersey.resource( getResourceUrl("/services/settings") );
        ClientResponse response = resource.get(ClientResponse.class);
        Assert.assertEquals(200, response.getStatus());
        // save some settings
        response = resource.post(ClientResponse.class, "{apiServerUsername: \"orestis\", apiServerPass: \"topsecret\"}");
        Assert.assertEquals(200, response.getStatus());
        // retrieve settings again
        resource = jersey.resource( getResourceUrl("/services/settings") );
        response = resource.get(ClientResponse.class);
        Assert.assertEquals(200, response.getStatus());
        // ...and check response
        String json = response.getEntity(String.class);
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(json).getAsJsonObject();
        Assert.assertEquals("Invalid settings apiServerUsername", "orestis", object.get("apiServerUsername").getAsString());
        Assert.assertEquals("Invalid settings apiServerPass", "topsecret", object.get("apiServerPass").getAsString());
    }

    @Deployment(name = "SettingsTest", managed = true, testable = false)
    public static WebArchive createWebArchive() {
        logger.info("Packaging Test App");
        logger.info("version");
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "restcomm-rvd.war");
        final WebArchive restcommArchive = ShrinkWrapMaven.resolver()
                .resolve("com.telestax.servlet:restcomm-rvd:war:" + version).withoutTransitivity()
                .asSingle(WebArchive.class);
        archive = archive.merge(restcommArchive);

        archive.addAsWebInfResource("restcomm.xml", "restcomm.xml");
        archive.delete("/WEB-INF/rvd.xml");
        archive.addAsWebInfResource("rvd.xml", "rvd.xml");

        logger.info("Packaged Test App");
        return archive;
    }
}
