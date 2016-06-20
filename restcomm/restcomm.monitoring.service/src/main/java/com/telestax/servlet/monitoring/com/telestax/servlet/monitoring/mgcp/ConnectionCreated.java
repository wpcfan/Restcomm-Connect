package com.telestax.servlet.monitoring.com.telestax.servlet.monitoring.mgcp;

import akka.actor.ActorRef;

/**
 * Created by gvagenas on 6/20/16.
 */
public class ConnectionCreated {
    private ActorRef connection;

    public ConnectionCreated(final ActorRef connection) {
        this.connection = connection;
    }

    public ActorRef getConnection() {
        return connection;
    }
}
