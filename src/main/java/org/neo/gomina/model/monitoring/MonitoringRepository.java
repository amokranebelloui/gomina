package org.neo.gomina.model.monitoring;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;

public class MonitoringRepository {

    private final static Logger logger = LogManager.getLogger(MonitoringRepository.class);

    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public EnvMonitoring getFor(String envName) {
        EnvMonitoring monitoring = new EnvMonitoring();
        try {
            File file = new File("data/mon." + envName + ".yaml");
            if (file.exists()) {
                List<Map<String, Object>> list = mapper.readValue(file, new TypeReference<List<Map<String, Object>>>(){});
                for (Map<String, Object> indicators : list) {
                    monitoring.add((String)indicators.get("name"), indicators);
                }
            }
        }
        catch (Exception e) {
            logger.error("", e);
        }
        return monitoring;
    }

}
