package com.telestax.servlet.monitoring.com.telestax.servlet.monitoring.mgcp;

import java.util.Map;

/**
 * Created by gvagenas on 6/20/16.
 */
public class MgcpMonitoringServiceResponse {
    private final Map<String, Integer> countersMap;


    public MgcpMonitoringServiceResponse(final Map<String, Integer> countersMap) {
        super();
        this.countersMap = countersMap;
    }

    public Map<String, Integer> getCountersMap() {
        return countersMap;
    }
}
