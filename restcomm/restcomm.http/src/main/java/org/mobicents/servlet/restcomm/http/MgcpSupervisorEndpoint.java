package org.mobicents.servlet.restcomm.http;

import akka.actor.ActorRef;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.telestax.servlet.monitoring.com.telestax.servlet.monitoring.mgcp.MgcpMonitoringService;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.mobicents.servlet.restcomm.dao.DaoManager;
import org.mobicents.servlet.restcomm.entities.RestCommResponse;
import org.mobicents.servlet.restcomm.http.converter.CallinfoConverter;
import org.mobicents.servlet.restcomm.http.converter.MonitoringServiceConverter;
import org.mobicents.servlet.restcomm.http.converter.RestCommResponseConverter;
import org.mobicents.servlet.restcomm.telephony.CallInfo;
import org.mobicents.servlet.restcomm.telephony.MonitoringServiceResponse;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

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

    public MgcpSupervisorEndpoint() {
        super();
    }

    @PostConstruct
    public void init() {
        mgcpMonitoringService = (ActorRef) context.getAttribute(MgcpMonitoringService.class.getName());
        configuration = (Configuration) context.getAttribute(Configuration.class.getName());
        configuration = configuration.subset("runtime-settings");
        daos = (DaoManager) context.getAttribute(DaoManager.class.getName());
        super.init(configuration);
        CallinfoConverter converter = new CallinfoConverter(configuration);
        MonitoringServiceConverter listConverter = new MonitoringServiceConverter(configuration);
        builder = new GsonBuilder();
        builder.registerTypeAdapter(CallInfo.class, converter);
        builder.registerTypeAdapter(MonitoringServiceResponse.class, listConverter);
        builder.setPrettyPrinting();
        gson = builder.create();
        xstream = new XStream();
        xstream.alias("RestcommResponse", RestCommResponse.class);
        xstream.registerConverter(converter);
        xstream.registerConverter(listConverter);
        xstream.registerConverter(new RestCommResponseConverter(configuration));
    }
}
