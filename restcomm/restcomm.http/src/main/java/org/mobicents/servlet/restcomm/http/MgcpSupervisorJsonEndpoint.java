package org.mobicents.servlet.restcomm.http;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * Created by gvagenas on 6/21/16.
 */
@Path("/Accounts/{accountSid}/MgcpSupervisor.json")
public class MgcpSupervisorJsonEndpoint extends MgcpSupervisorEndpoint {

    public MgcpSupervisorJsonEndpoint() {
        super();
    }

    @Path("/metrics")
    @GET
    public Response getMetrics(@PathParam("accountSid") final String accountSid) {
        return getMetrics(accountSid, APPLICATION_JSON_TYPE);
    }
}
