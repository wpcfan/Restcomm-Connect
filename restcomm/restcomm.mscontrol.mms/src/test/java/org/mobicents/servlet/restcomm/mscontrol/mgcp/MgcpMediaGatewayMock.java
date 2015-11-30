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

package org.mobicents.servlet.restcomm.mscontrol.mgcp;

import java.net.InetAddress;

import org.mobicents.servlet.restcomm.mgcp.MediaGateway;
import org.mobicents.servlet.restcomm.mscontrol.MediaGatewayMock;
import org.mobicents.servlet.restcomm.mscontrol.MediaServerController;
import org.mobicents.servlet.restcomm.mscontrol.MediaServerInfo;

import akka.actor.ActorSystem;

/**
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 *
 */
public class MgcpMediaGatewayMock extends MediaGatewayMock {
    
    private final ActorSystem actorSystem;
    private final MediaGateway mediaGateway;
    
    public MgcpMediaGatewayMock(String name, InetAddress address, int port, int timeout) {
        this.actorSystem = ActorSystem.create("MGCP Media GW");
        this.mediaGateway = new MediaGateway();
        this.info = new MediaServerInfo(name, address, port, timeout);
        this.controllerFactory = new MmsControllerFactory(actorSystem, getCon);
    }

    @Override
    public MediaServerController getCallController() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MediaServerController getBridgeController() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MediaServerController getConferenceController() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        // TODO Auto-generated method stub
        
    }

}
