package org.neo.gomina.model.monitoring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Monitoring {

    private final static Logger logger = LogManager.getLogger(Monitoring.class);

    private Map<String, EnvMonitoring> topology = new ConcurrentHashMap<>();

    private List<MonitoringListener> listeners = new CopyOnWriteArrayList<>();

    public void add(MonitoringListener listener) {
        this.listeners.add(listener);
    }

    public void notify(String env, String instanceId, Map<String, Object> newValues) {
        try {
            EnvMonitoring envMonitoring = topology.computeIfAbsent(env, (k) -> new EnvMonitoring());
            Map<String, Object> indicators = envMonitoring.getForInstance(instanceId);
            logger.trace("Notify {}", newValues);
            for (Map.Entry<String, Object> entry : newValues.entrySet()) {
                if (entry.getValue() != null) {
                    indicators.put(entry.getKey(), entry.getValue());
                }
                else {
                    indicators.remove(entry.getKey());
                }
            }

            for (MonitoringListener listener : listeners) {
                listener.onPropertyChanged(env, instanceId, newValues);
            }
        }
        catch (Exception e) {
            logger.error("Cannot notify env={} instance={}", env, instanceId, e);
        }
    }

    public EnvMonitoring getFor(String env) {
        return topology.getOrDefault(env, new EnvMonitoring());
    }
}
