package org.neo.gomina.model.monitoring;

import java.util.HashMap;
import java.util.Map;

public class EnvMonitoring {

    Map<String, Map<String, Object>> map = new HashMap<>(); // instance.id / indicators

    public Map<String, Object> getForInstance(String name) {
        return map.computeIfAbsent(name, (k) -> new HashMap<>());
    }
}
