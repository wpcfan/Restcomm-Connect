package com.telestax.servlet.monitoring.com.telestax.servlet.monitoring.mgcp;

import akka.actor.ActorRef;

/**
 * Created by gvagenas on 6/20/16.
 */
public class LinkCreated {
    private ActorRef link;

    public LinkCreated (final ActorRef link) {
        this.link = link;
    }

    public ActorRef getLink() {
        return link;
    }
}
