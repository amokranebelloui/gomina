package org.neo.gomina.model.monitoring.dummy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.monitoring.EnvMonitoring;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DummyMonitorData {

    private final static Logger logger = LogManager.getLogger(DummyMonitorData.class);

    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public Map<String, Map<String, Object>> getFor(String envName) {
        Map<String, Map<String, Object>> monitoring = new HashMap<>();
        try {
            File file = new File("data/mon." + envName + ".yaml");
            if (file.exists()) {
                List<Map<String, Object>> list = mapper.readValue(file, new TypeReference<List<Map<String, Object>>>(){});
                for (Map<String, Object> indicators : list) {
                    monitoring.put((String)indicators.get("name"), indicators);
                }
            }
        }
        catch (Exception e) {
            logger.error("", e);
        }
        return monitoring;
    }

}
