package org.neo.gomina.model.sonar.dummy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.sonar.SonarConnector;
import org.neo.gomina.model.sonar.SonarIndicators;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DummySonarConnector implements SonarConnector {

    private final static Logger logger = LogManager.getLogger(DummySonarConnector.class);

    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    @Override
    public Map<String, SonarIndicators> getMetrics() {
        return getMetrics(null);
    }

    @Override
    public Map<String, SonarIndicators> getMetrics(String resource) {
        Map<String, SonarIndicators> indicators = new HashMap<>();
        try {
            List<SonarIndicators> projects = mapper.readValue(new File("data/projects.sonar.yaml"), new TypeReference<List<SonarIndicators>>() {});
            for (SonarIndicators project : projects) {
                indicators.put(project.code, project);
            }
        }
        catch (IOException e) {
            logger.error("Error retrieving Sonar data", e);
        }
        return indicators;
    }
}
