package com.telestax.servlet.monitoring.com.telestax.servlet.monitoring.mgcp;

import java.util.Map;

/**
 * Created by gvagenas on 6/20/16.
 */
public class MgcpMonitoringServiceResponse {
    private final Map<String, Integer> countersMap;
    private final Map<String, Integer> endpointDetailsMap;
    private int liveCalls = 0;


    public MgcpMonitoringServiceResponse(final Map<String, Integer> countersMap, final Map<String, Integer> endpointDetailsMap) {
        super();
        this.countersMap = countersMap;
        this.endpointDetailsMap = endpointDetailsMap;
    }

    public Map<String, Integer> getCountersMap() {
        return countersMap;
    }

    public Map<String, Integer> getEndpointDetailsMap() {
        return endpointDetailsMap;
    }

    public void setLiveCalls(int liveCalls) {
        this.liveCalls = liveCalls;
    }

    public int getLiveCalls() {
        return liveCalls;
    }
}
