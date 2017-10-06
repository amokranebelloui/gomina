package org.neo.gomina.model.monitoring;

import java.util.Map;

public interface MonitoringListener {

    void onPropertyChanged(String env, String instanceId, Map<String, Object> newValues);

}
