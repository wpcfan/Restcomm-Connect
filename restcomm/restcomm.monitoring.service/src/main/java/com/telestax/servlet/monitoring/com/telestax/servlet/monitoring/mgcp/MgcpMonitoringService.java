package com.telestax.servlet.monitoring.com.telestax.servlet.monitoring.mgcp;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.mobicents.servlet.restcomm.mgcp.ConnectionCreated;
import org.mobicents.servlet.restcomm.mgcp.DestroyConnection;
import org.mobicents.servlet.restcomm.mgcp.DestroyEndpoint;
import org.mobicents.servlet.restcomm.mgcp.DestroyLink;
import org.mobicents.servlet.restcomm.mgcp.EndpointCreated;
import org.mobicents.servlet.restcomm.mgcp.EndpointCreated.ENDPOINT_TYPE;
import org.mobicents.servlet.restcomm.mgcp.LinkCreated;
import org.mobicents.servlet.restcomm.telephony.GetMgcpMetrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by gvagenas on 6/20/16.
 */
public class MgcpMonitoringService extends UntypedActor{

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private final Map<ActorRef, ENDPOINT_TYPE> endpoints;
    private final List<ActorRef> links;
    private final List<ActorRef> connections;
    private final AtomicInteger confEndpoints;
    private final AtomicInteger packetRelayEndpoints;
    private final AtomicInteger bridgeEndpoints;
    private final AtomicInteger ivrEndpoints;

    public MgcpMonitoringService() {
        endpoints =  new ConcurrentHashMap<ActorRef, ENDPOINT_TYPE>();
        links = Collections.synchronizedList(new ArrayList());
        connections = Collections.synchronizedList(new ArrayList());
        confEndpoints = new AtomicInteger();
        packetRelayEndpoints = new AtomicInteger();
        bridgeEndpoints = new AtomicInteger();
        ivrEndpoints = new AtomicInteger();
    }

    public void putEndpoint(final EndpointCreated endpointCreated) {
        endpoints.put(endpointCreated.getEndpoint(), endpointCreated.getType());
        switch (endpointCreated.getType()) {
            case BRIDGE_ENDPOINT:
                bridgeEndpoints.incrementAndGet();
                break;
            case IVR_ENDPOINT:
                ivrEndpoints.incrementAndGet();
                break;
            case CONFERENCE_ENDPOINT:
                confEndpoints.incrementAndGet();
                break;
            case PACKETRELAY_ENDPOINT:
                packetRelayEndpoints.incrementAndGet();
                break;
            default:
                break;
        }
    }

    public void removeEndpoint(final ActorRef actorRef) {
        ENDPOINT_TYPE type = endpoints.remove(actorRef);
        switch (type) {
            case BRIDGE_ENDPOINT:
                bridgeEndpoints.decrementAndGet();
                break;
            case IVR_ENDPOINT:
                ivrEndpoints.decrementAndGet();
                break;
            case CONFERENCE_ENDPOINT:
                confEndpoints.decrementAndGet();
                break;
            case PACKETRELAY_ENDPOINT:
                packetRelayEndpoints.decrementAndGet();
                break;
            default:
                break;
        }
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
        Map<String, Integer> endpointDetails = new ConcurrentHashMap<String, Integer>();

        countersMap.put("Endpoints",endpoints.size());
        countersMap.put("Connections",connections.size());
        countersMap.put("Links",links.size());
        if (endpoints.size() > 0) {
            endpointDetails.put("Conference Endpoints",confEndpoints.get());
            endpointDetails.put("Ivr Endpoints", ivrEndpoints.get());
            endpointDetails.put("Bridge Endpoint", bridgeEndpoints.get());
            endpointDetails.put("Packet relay Endpoints", packetRelayEndpoints.get());
        }
        MgcpMonitoringServiceResponse mgcpMonitoringServiceResponse = new MgcpMonitoringServiceResponse(countersMap, endpointDetails);
        sender.tell(mgcpMonitoringServiceResponse, self());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        final Class<?> klass = message.getClass();
        final ActorRef self = self();
        final ActorRef sender = sender();
        if(logger.isInfoEnabled()){
            logger.info("MgcpMonitoringService Processing Message: \"" + klass.getName() + " sender : "+ sender.getClass()+" self is terminated: "+self.isTerminated());
        }
        if (EndpointCreated.class.equals(klass)) {
            putEndpoint((EndpointCreated) message);
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