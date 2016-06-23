package org.mobicents.servlet.restcomm.tools;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.apache.log4j.Logger;

import javax.ws.rs.core.MediaType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertNotNull;

/**
 * Created by gvagenas on 6/21/16.
 */
public class MgcpMonitoringServiceTool {
    private static MgcpMonitoringServiceTool instance;
    private static Logger logger = Logger.getLogger(MgcpMonitoringServiceTool.class);

    private MgcpMonitoringServiceTool() {}

    public static MgcpMonitoringServiceTool getInstance() {
        if (instance == null)
            instance = new MgcpMonitoringServiceTool();

        return instance;
    }

    private String getAccountsUrl(String deploymentUrl, String username) {
        int registeredUsers;
        if (deploymentUrl.endsWith("/")) {
            deploymentUrl = deploymentUrl.substring(0, deploymentUrl.length() - 1);
        }

        return deploymentUrl + "/2012-04-24/Accounts/" + username + "/MgcpSupervisor.json";
    }

    private JsonObject getMgcpResponse(String deploymentUrl, String username, String authToken) {
        Client jerseyClient = Client.create();
        jerseyClient.addFilter(new HTTPBasicAuthFilter(username, authToken));

        String url = getAccountsUrl(deploymentUrl, username);

        WebResource webResource = jerseyClient.resource(url);

        String response = null;

        response = webResource.path("/metrics").accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML).get(String.class);

        JsonParser parser = new JsonParser();

        JsonObject jsonObject = parser.parse(response).getAsJsonObject();

        return jsonObject;
    }

    public JsonObject getMgcpMetrics(String deploymentUrl, String username, String authToken) {
        JsonObject jsonObject = getMgcpResponse(deploymentUrl,username,authToken);
        JsonObject metrics = jsonObject.getAsJsonObject("Metrics");
        return metrics;
    }

    public JsonObject getMgcpEndpointDetails(String deploymentUrl, String username, String authToken) {
        JsonObject jsonObject = getMgcpResponse(deploymentUrl,username,authToken);
        JsonObject endpointDetails = jsonObject.getAsJsonObject("EndpointDetails");
        return endpointDetails;
    }

    public Map<String, Integer> getMgcpMetricsMap(final String deploymentUrl, final String adminAccountSid, final String adminAuthToken) {
        Map<String, Integer> mgcpMetricsMap = new ConcurrentHashMap<>();

        JsonObject mgcpMetrics = getMgcpMetrics(deploymentUrl.toString(), adminAccountSid, adminAuthToken);
        assertNotNull(mgcpMetrics);
        mgcpMetricsMap.put("LiveCalls", mgcpMetrics.get("LiveCalls").getAsInt());
        mgcpMetricsMap.put("Links", mgcpMetrics.get("Links").getAsInt());
        mgcpMetricsMap.put("Connections", mgcpMetrics.get("Connections").getAsInt());
        mgcpMetricsMap.put("Endpoint", mgcpMetrics.get("Endpoints").getAsInt());

        return mgcpMetricsMap;
    }

    public boolean assertMgcpMetrics(final String deploymentUrl, final String adminAccountSid, final String adminAuthToken) {
        Map<String, Integer> mgcpMetricsMap = getMgcpMetricsMap(deploymentUrl, adminAccountSid, adminAuthToken);
        int liveCalls = mgcpMetricsMap.get("LiveCalls");
        int links = mgcpMetricsMap.get("Links");
        int connections = mgcpMetricsMap.get("Connections");
        int endpoints = mgcpMetricsMap.get("Endpoints");
        if (liveCalls != 0 || endpoints != 0) {
            JsonObject endpointDetails = MgcpMonitoringServiceTool.getInstance().getMgcpEndpointDetails(deploymentUrl.toString(),adminAccountSid, adminAuthToken);
            //Just to add breakpoint during debug so you can examine which endpoints are left in memory
            logger.info("Mgcp Metrics, LiveCalls: "+liveCalls+" Links: "+links+" Connections: "+connections+" Endpoints: "+endpoints);
            if (endpointDetails != null) {
                logger.info("Conference Endpoints: "+endpointDetails.get("Conference Endpoints").getAsInt());
                logger.info("Ivr Endpoints: "+endpointDetails.get("Ivr Endpoints").getAsInt());
                logger.info("Bridge Endpoint: "+endpointDetails.get("Bridge Endpoint").getAsInt());
                logger.info("Packet relay Endpoints: "+endpointDetails.get("Packet relay Endpoints").getAsInt());
            }
            return false;
        } else if ((liveCalls == 0) && (links == 0) && (connections == 0) && (endpoints == 0)) {
            return true;
        } else {
            logger.info("Mgcp Metrics, LiveCalls: "+liveCalls+" Links: "+links+" Connections: "+connections+" Endpoints: "+endpoints);
            return false;
        }
    }
}
