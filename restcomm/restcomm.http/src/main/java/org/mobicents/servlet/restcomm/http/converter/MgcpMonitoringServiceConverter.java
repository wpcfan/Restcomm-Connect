package org.mobicents.servlet.restcomm.http.converter;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.telestax.servlet.monitoring.com.telestax.servlet.monitoring.mgcp.MgcpMonitoringServiceResponse;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.commons.configuration.Configuration;

import java.lang.reflect.Type;

/**
 * Created by gvagenas on 6/20/16.
 */
public class MgcpMonitoringServiceConverter extends AbstractConverter implements JsonSerializer<MgcpMonitoringServiceResponse> {

    public MgcpMonitoringServiceConverter(Configuration configuration) {
        super(configuration);
    }

    @Override
    public JsonElement serialize(MgcpMonitoringServiceResponse mgcpMonitoringServiceResponse, Type type, JsonSerializationContext jsonSerializationContext) {
        return null;
    }

    @Override
    public boolean canConvert(Class klass) {
        return false;
    }

    @Override
    public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {

    }
}
