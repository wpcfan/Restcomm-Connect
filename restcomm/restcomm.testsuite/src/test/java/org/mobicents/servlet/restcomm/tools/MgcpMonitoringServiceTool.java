package org.mobicents.servlet.restcomm.tools;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.apache.log4j.Logger;

import javax.ws.rs.core.MediaType;

/**
 * Created by gvagenas on 6/21/16.
 */
public class MgcpMonitoringServiceTool {
    private static MgcpMonitoringServiceTool instance;
    private static String accountsUrl;
    private static Logger logger = Logger.getLogger(MgcpMonitoringServiceTool.class);

    private MgcpMonitoringServiceTool() {}

    public static MgcpMonitoringServiceTool getInstance() {
        if (instance == null)
            instance = new MgcpMonitoringServiceTool();

        return instance;
    }

    private String getAccountsUrl(String deploymentUrl, String username) {
        int registeredUsers;
        if (accountsUrl == null) {
            if (deploymentUrl.endsWith("/")) {
                deploymentUrl = deploymentUrl.substring(0, deploymentUrl.length() - 1);
            }
            accountsUrl = deploymentUrl + "/2012-04-24/Accounts/" + username + "/MgcpSupervisor.json";
        }
        return accountsUrl;
    }

    public JsonObject getMgcpMetrics(String deploymentUrl, String username, String authToken) {
        Client jerseyClient = Client.create();
        jerseyClient.addFilter(new HTTPBasicAuthFilter(username, authToken));

        String url = getAccountsUrl(deploymentUrl, username);

        WebResource webResource = jerseyClient.resource(url);

        String response = null;

        response = webResource.path("/metrics").accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML).get(String.class);

        JsonParser parser = new JsonParser();

        JsonObject jsonObject = parser.parse(response).getAsJsonObject();

        JsonObject metrics = jsonObject.getAsJsonObject("Metrics");

        return metrics;
    }
}
