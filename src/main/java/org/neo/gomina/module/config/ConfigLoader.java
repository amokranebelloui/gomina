package org.neo.gomina.module.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.module.kotlin.KotlinModule;

import java.io.File;

public class ConfigLoader {

    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
            .registerModule(new KotlinModule());

    public ConfigLoader() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Config load() throws Exception {
        File configFile = new File("config/config.yaml");
        return mapper.readValue(configFile, Config.class);
    }
}
