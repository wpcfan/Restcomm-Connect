package com.telestax.servlet.monitoring.com.telestax.servlet.monitoring.mgcp;

import akka.actor.ActorRef;

/**
 * Created by gvagenas on 6/20/16.
 */
public class EndpointCreated {
    private ActorRef endpoint;

    public EndpointCreated(final ActorRef endpoint) {
        this.endpoint = endpoint;
    }

    public ActorRef getEndpoint() {
        return endpoint;
    }
}
