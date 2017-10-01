package org.neo.gomina.model.monitoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnvMonitoring {

    Map<String, Map<String, Object>> map = new HashMap<>(); // id / values

    public void add(String name, Map<String, Object> indicators) {
        map.put(name, indicators);
    }

    public List<String> getInstances() {
        return new ArrayList<>(map.keySet());
    }

    public Map<String, Object> getForInstance(String name) {
        return map.get(name);
    }
}
