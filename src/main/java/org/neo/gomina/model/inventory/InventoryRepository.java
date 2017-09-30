package org.neo.gomina.model.inventory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InventoryRepository {

    private final static Logger logger = LogManager.getLogger(InventoryRepository.class);

    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    private Pattern pattern = Pattern.compile("env\\.(.*?)\\.yaml");

    public InventoryRepository() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<String> getEnvs() {
        List<String> envs = new ArrayList<>();
        File data = new File("data");
        if (data.isDirectory()) {
            for (File file : data.listFiles()) {
                Matcher matcher = pattern.matcher(file.getName());
                if (matcher.find()) {
                    envs.add(matcher.group(1));
                }
            }
        }
        return envs;
    }

    public Environment getEnvironment(String envName) {
        Environment env = null;
        try {
            env = mapper.readValue(new File("data/env." + envName + ".yaml"), Environment.class);
        }
        catch (Exception e) {
            logger.error("", e);
        }
        return env;
    }

}
