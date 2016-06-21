package org.mobicents.servlet.restcomm.http;

import akka.actor.ActorRef;
import akka.util.Timeout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.telestax.servlet.monitoring.MonitoringService;
import com.telestax.servlet.monitoring.com.telestax.servlet.monitoring.mgcp.MgcpMonitoringService;
import com.telestax.servlet.monitoring.com.telestax.servlet.monitoring.mgcp.MgcpMonitoringServiceResponse;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.mobicents.servlet.restcomm.dao.DaoManager;
import org.mobicents.servlet.restcomm.entities.RestCommResponse;
import org.mobicents.servlet.restcomm.http.converter.MgcpMonitoringServiceConverter;
import org.mobicents.servlet.restcomm.http.converter.RestCommResponseConverter;
import org.mobicents.servlet.restcomm.telephony.GetLiveCalls;
import org.mobicents.servlet.restcomm.telephony.GetMgcpMetrics;
import org.mobicents.servlet.restcomm.telephony.MonitoringServiceResponse;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;

/**
 * Created by gvagenas on 6/20/16.
 */
public class MgcpSupervisorEndpoint extends SecuredEndpoint{
    private static Logger logger = Logger.getLogger(SupervisorEndpoint.class);

    @Context
    protected ServletContext context;
    protected Configuration configuration;
    private DaoManager daos;
    private Gson gson;
    private GsonBuilder builder;
    private XStream xstream;
    private ActorRef mgcpMonitoringService;
    private ActorRef monitoringService;

    public MgcpSupervisorEndpoint() {
        super();
    }

    @PostConstruct
    public void init() {
        mgcpMonitoringService = (ActorRef) context.getAttribute(MgcpMonitoringService.class.getName());
        monitoringService = (ActorRef) context.getAttribute(MonitoringService.class.getName());
        configuration = (Configuration) context.getAttribute(Configuration.class.getName());
        configuration = configuration.subset("runtime-settings");
        daos = (DaoManager) context.getAttribute(DaoManager.class.getName());
        super.init(configuration);
        MgcpMonitoringServiceConverter mgcpResponseConverter = new MgcpMonitoringServiceConverter(configuration);
        builder = new GsonBuilder();
        builder.registerTypeAdapter(MgcpMonitoringServiceResponse.class, mgcpResponseConverter);
        builder.setPrettyPrinting();
        gson = builder.create();
        xstream = new XStream();
        xstream.alias("RestcommResponse", RestCommResponse.class);
        xstream.registerConverter(mgcpResponseConverter);
        xstream.registerConverter(new RestCommResponseConverter(configuration));
    }

    protected Response getMetrics(final String accountSid, MediaType responseType) {
        secure(daos.getAccountsDao().getAccount(accountSid), "RestComm:Read:Calls");
        //Get the list of live calls from Monitoring Service
        MgcpMonitoringServiceResponse mgcpMonitoringServiceResponse;
        MonitoringServiceResponse monitoringServiceResponse;
        try {
            final Timeout expires = new Timeout(Duration.create(60, TimeUnit.SECONDS));
            final GetMgcpMetrics getMgcpMetrics = new GetMgcpMetrics();
            Future<Object> future = (Future<Object>) ask(mgcpMonitoringService, getMgcpMetrics, expires);
            mgcpMonitoringServiceResponse = (MgcpMonitoringServiceResponse) Await.result(future, Duration.create(10, TimeUnit.SECONDS));

            final GetLiveCalls getLiveCalls = new GetLiveCalls();
            future = (Future<Object>) ask(monitoringService, getLiveCalls, expires);
            monitoringServiceResponse = (MonitoringServiceResponse) Await.result(future, Duration.create(10, TimeUnit.SECONDS));
            mgcpMonitoringServiceResponse.setLiveCalls(monitoringServiceResponse.getCountersMap().get("LiveCalls").intValue());
        } catch (Exception exception) {
            return status(BAD_REQUEST).entity(exception.getMessage()).build();
        }
        if (mgcpMonitoringServiceResponse != null) {
            if (APPLICATION_XML_TYPE == responseType) {
                final RestCommResponse response = new RestCommResponse(mgcpMonitoringServiceResponse);
                return ok(xstream.toXML(response), APPLICATION_XML).build();
            } else if (APPLICATION_JSON_TYPE == responseType) {
                Response response = ok(gson.toJson(mgcpMonitoringServiceResponse), APPLICATION_JSON).build();
                if(logger.isDebugEnabled()){
                    logger.debug("Supervisor endpoint response: "+gson.toJson(mgcpMonitoringServiceResponse));
                }
                return response;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
