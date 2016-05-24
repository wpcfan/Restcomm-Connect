/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.servlet.restcomm.util;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.mobicents.servlet.restcomm.annotations.concurrency.ThreadSafe;

/**
 * @author guilherme.jansen@telestax.com
 */
@ThreadSafe
public class OrganizationUtils {

    private OrganizationUtils(){
        super();
    }

    /**
     * Extracts the Organization namespace from a host string
     *
     * @param host
     * @return null for IPv4 and IPv6 hosts or a Organization namespace for DNS hosts
     */
    public static String getOrganizationNamespace(String host) {
        final InetAddressValidator addressValidator = InetAddressValidator.getInstance();
        if (host.contains("[") || host.contains("]"))
            // Remove ipv6 brackets if present
            host = host.replace("[", "").replace("]", "");
        if (!addressValidator.isValidInet4Address(host) && !addressValidator.isValidInet6Address(host)
                && !host.equalsIgnoreCase("localhost")) {
            // Assuming host as a domain name, proceed extracting namespace
            final String namespace = host.split("\\.")[0];
            return namespace;
        }
        return null;
    }


}
