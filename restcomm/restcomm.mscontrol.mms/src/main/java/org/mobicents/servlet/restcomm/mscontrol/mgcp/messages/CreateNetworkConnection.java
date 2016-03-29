/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2013, Telestax Inc and individual contributors
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

package org.mobicents.servlet.restcomm.mscontrol.mgcp.messages;

import jain.protocol.ip.mgcp.message.parms.ConnectionMode;

/**
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 *
 */
public final class CreateNetworkConnection {

    private final String sessionDescription;
    private final ConnectionMode connectionMode;
    private final boolean outbound;
    private final boolean webrtc;

    public CreateNetworkConnection(String sessionDescription, ConnectionMode mode, boolean webrtc, boolean outbound) {
        this.sessionDescription = sessionDescription;
        this.connectionMode = mode;
        this.outbound = outbound;
        this.webrtc = webrtc;
    }

    public String getSessionDescription() {
        return sessionDescription;
    }

    public ConnectionMode getConnectionMode() {
        return connectionMode;
    }

    public boolean isWebrtc() {
        return webrtc;
    }

    public boolean isOutbound() {
        return outbound;
    }

}
