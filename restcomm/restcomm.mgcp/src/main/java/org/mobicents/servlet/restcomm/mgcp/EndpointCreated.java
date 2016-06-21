package org.mobicents.servlet.restcomm.mgcp;

import akka.actor.ActorRef;

/**
 * Created by gvagenas on 6/20/16.
 */
public class EndpointCreated {
    public static enum ENDPOINT_TYPE { IVR_ENDPOINT, CONFERENCE_ENDPOINT, PACKETRELAY_ENDPOINT, BRIDGE_ENDPOINT}
    private final ActorRef endpoint;
    private final ENDPOINT_TYPE type;

    public EndpointCreated(final ActorRef endpoint, final ENDPOINT_TYPE type) {
        this.endpoint = endpoint;
        this.type = type;
    }

    public ActorRef getEndpoint() {
        return endpoint;
    }

    public ENDPOINT_TYPE getType() {
        return type;
    }
}
