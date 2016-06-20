package com.telestax.servlet.monitoring.com.telestax.servlet.monitoring.mgcp;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.mobicents.servlet.restcomm.mgcp.DestroyConnection;
import org.mobicents.servlet.restcomm.mgcp.DestroyEndpoint;
import org.mobicents.servlet.restcomm.mgcp.DestroyLink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gvagenas on 6/20/16.
 */
public class MgcpMonitoringService extends UntypedActor{

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private final List<ActorRef> endpoints = Collections.synchronizedList(new ArrayList());
    private final List<ActorRef> links  = Collections.synchronizedList(new ArrayList());
    private final List<ActorRef> connections = Collections.synchronizedList(new ArrayList());

    public MgcpMonitoringService() {
//        endpoints = new ConcurrentHashMap<MediaSession, ActorRef>();
//        links = new ConcurrentHashMap<MediaSession, ActorRef>();
//        connections = new ConcurrentHashMap<MediaSession, ActorRef>();
    }

    public void putEndpoint(final ActorRef actorRef) {
        endpoints.add(actorRef);
    }

    public void removeEndpoint(final ActorRef actorRef) {
        endpoints.remove(actorRef);
    }

    public void putConnection(final ActorRef actorRef) {
        connections.add(actorRef);
    }

    public void removeConnection(final ActorRef actorRef) {
        connections.remove(actorRef);
    }

    public void putLink(final ActorRef actorRef) {
        links.add(actorRef);
    }

    public void removeLink(final ActorRef actorRef) {
        links.remove(actorRef);
    }

    public void getMetrics(final ActorRef sender) {
        Map<String, Integer> countersMap = new HashMap<String, Integer>();
        countersMap.put("Endpoints",endpoints.size());
        countersMap.put("Connections",connections.size());
        countersMap.put("Links",links.size());
        MgcpMonitoringServiceResponse mgcpMonitoringServiceResponse = new MgcpMonitoringServiceResponse(countersMap);
        sender.tell(mgcpMonitoringServiceResponse, self());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        final Class<?> klass = message.getClass();
        final ActorRef self = self();
        final ActorRef sender = sender();
        if(logger.isInfoEnabled()){
            logger.info("MonitoringService Processing Message: \"" + klass.getName() + " sender : "+ sender.getClass()+" self is terminated: "+self.isTerminated());
        }
        if (EndpointCreated.class.equals(klass)) {
            putEndpoint(((EndpointCreated)message).getEndpoint());
        } else if (ConnectionCreated.class.equals(klass)) {
            putConnection(((ConnectionCreated)message).getConnection());
        } else if (LinkCreated.class.equals(klass)) {
            putLink(((LinkCreated)message).getLink());
        } else if (DestroyConnection.class.equals(klass)) {
            removeConnection(((DestroyConnection)message).connection());
        } else if (DestroyLink.class.equals(klass)) {
            removeLink(((DestroyLink)message).link());
        } else if (DestroyEndpoint.class.equals(klass)) {
            removeEndpoint(((DestroyEndpoint)message).endpoint());
        } else if (GetMgcpMetrics.class.equals(klass)) {
            getMetrics(sender);
        }
    }
}
