package org.neo.gomina.model.monitoring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EnvMonitoring {

    Map<String, Map<String, Object>> map = new ConcurrentHashMap<>(); // instance.id / indicators

    public Map<String, Object> getForInstance(String name) {
        return map.computeIfAbsent(name, (k) -> new ConcurrentHashMap<>());
    }

    public Map<String, Map<String, Object>> getAll() {
        return map;
    }
}
