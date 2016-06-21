package org.mobicents.servlet.restcomm.http.converter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.telestax.servlet.monitoring.com.telestax.servlet.monitoring.mgcp.MgcpMonitoringServiceResponse;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.commons.configuration.Configuration;
import org.mobicents.servlet.restcomm.configuration.RestcommConfiguration;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by gvagenas on 6/20/16.
 */
public class MgcpMonitoringServiceConverter extends AbstractConverter implements JsonSerializer<MgcpMonitoringServiceResponse> {

    public MgcpMonitoringServiceConverter(Configuration configuration) {
        super(configuration);
    }

    @Override
    public JsonElement serialize(MgcpMonitoringServiceResponse mgcpMonitoringServiceResponse, Type type, JsonSerializationContext jsonSerializationContext) {
        Map<String, Integer> countersMap = mgcpMonitoringServiceResponse.getCountersMap();
        Map<String, Integer> endpointDetailsMap = mgcpMonitoringServiceResponse.getEndpointDetailsMap();

        JsonObject result = new JsonObject();
        JsonObject metrics = new JsonObject();
        JsonObject endpointDetails = new JsonObject();

        //First add InstanceId and Version details
        result.addProperty("InstanceId", RestcommConfiguration.getInstance().getMain().getInstanceId().toString());
        result.addProperty("Version", org.mobicents.servlet.restcomm.Version.getVersion());
        result.addProperty("Revision", org.mobicents.servlet.restcomm.Version.getRevision());

        metrics.addProperty("LiveCalls",mgcpMonitoringServiceResponse.getLiveCalls());

        Iterator<String> counterIterator = countersMap.keySet().iterator();
        while (counterIterator.hasNext()) {
            String counter = counterIterator.next();
            metrics.addProperty(counter, countersMap.get(counter));
        }
        result.add("Metrics", metrics);

        if (endpointDetailsMap.size() > 0) {
            Iterator<String> endpointDetailsIter = endpointDetailsMap.keySet().iterator();
            while (endpointDetailsIter.hasNext()) {
                String endpointType = endpointDetailsIter.next();
                endpointDetails.addProperty(endpointType, endpointDetailsMap.get(endpointType));
            }
            result.add("EndpointDetails", endpointDetails);
        }
        return result;
    }

    @Override
    public boolean canConvert(Class klass) {
        return MgcpMonitoringServiceResponse.class.equals(klass);
    }

    @Override
    public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {
        final MgcpMonitoringServiceResponse mgcpMonitoringServiceResponse = (MgcpMonitoringServiceResponse) object;
        final Map<String, Integer> countersMap = mgcpMonitoringServiceResponse.getCountersMap();
        Map<String, Integer> endpointDetailsMap = mgcpMonitoringServiceResponse.getEndpointDetailsMap();

        Iterator<String> counterIterator = countersMap.keySet().iterator();

        writer.startNode("InstanceId");
        writer.setValue(RestcommConfiguration.getInstance().getMain().getInstanceId().toString());
        writer.endNode();

        writer.startNode("Version");
        writer.setValue(org.mobicents.servlet.restcomm.Version.getVersion());
        writer.endNode();

        writer.startNode("Revision");
        writer.setValue(org.mobicents.servlet.restcomm.Version.getRevision());
        writer.endNode();

        writer.startNode("Metrics");

        writer.startNode("LiveCalls");
        writer.setValue(String.valueOf(mgcpMonitoringServiceResponse.getLiveCalls()));
        writer.endNode();

        while (counterIterator.hasNext()) {
            String counter = counterIterator.next();
            writer.startNode(counter);
            writer.setValue(String.valueOf(countersMap.get(counter)));
            writer.endNode();
        }
        writer.endNode();

        if (endpointDetailsMap.size() > 0) {
            writer.startNode("EndpointDetails");
            Iterator<String> endpointDetailsIter = endpointDetailsMap.keySet().iterator();
            while (endpointDetailsIter.hasNext()) {
                String endpointType = endpointDetailsIter.next();
                writer.startNode(endpointType);
                writer.setValue(String.valueOf(endpointDetailsMap.get(endpointType)));
                writer.endNode();
            }
            writer.endNode();
        }
    }
}
