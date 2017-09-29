package org.neo.gomina.model.sonar;

import java.util.List;
import java.util.Map;

public class SonarProject {

    private String key;
    private List<Map<String, String>> msr;

    public String getKey() {
        return key;
    }

    public List<Map<String, String>> getMsr() {
        return msr;
    }

    public String getMetric(List<Map<String, String>> msr, String metric) {
        for (Map<String, String> m : msr) {
            if (metric != null && metric.equals(m.get("key"))) {
                return m.get("val");
            }
        }
        return null;
    }
}
