package org.neo.gomina.model.monitoring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

// TODO Explore reactive streams
public class Monitoring {

    private Map<String, EnvMonitoring> topology = new HashMap<>();

    private List<MonitoringListener> listeners = new CopyOnWriteArrayList<>();

    public void add(MonitoringListener listener) {
        this.listeners.add(listener);
    }

    public void notify(String env, String instanceId, Map<String, Object> newValues) {
        EnvMonitoring envMonitoring = topology.computeIfAbsent(env, (k) -> new EnvMonitoring());
        Map<String, Object> indicators = envMonitoring.getForInstance(instanceId);
        indicators.putAll(newValues);

        for (MonitoringListener listener : listeners) {
            listener.onPropertyChanged(env, instanceId, newValues);
        }
    }

    public EnvMonitoring getFor(String envName) {
        return topology.getOrDefault(envName, new EnvMonitoring());
    }
}
