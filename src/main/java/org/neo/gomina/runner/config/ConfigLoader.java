package org.neo.gomina.runner.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;

public class ConfigLoader {

    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public ConfigLoader() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Config load() throws Exception {
        File configFile = new File("config/config.yaml");
        return mapper.readValue(configFile, Config.class);
    }
}