package org.neo.gomina.model.monitoring;

import java.util.Map;

public interface MonitoringListener {

    void onPropertyChanged(String instanceId, Map<String, String> newValues);

}
