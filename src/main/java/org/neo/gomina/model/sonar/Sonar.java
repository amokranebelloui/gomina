package org.neo.gomina.model.sonar;

import java.util.Map;

public interface Sonar {

    Map<String, SonarIndicators> getMetrics();
    Map<String, SonarIndicators> getMetrics(String resource);

}
