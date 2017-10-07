package org.neo.gomina.model.inventory.file;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.inventory.Environment;
import org.neo.gomina.model.inventory.Inventory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileInventory implements Inventory {

    private final static Logger logger = LogManager.getLogger(FileInventory.class);

    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    private Pattern pattern = Pattern.compile("env\\.(.*?)\\.yaml");

    public FileInventory() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private List<String> getFileCodes() {
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

    public Map<String, Environment> getAllEnvironments() {
        Map<String, Environment> environments = new HashMap<>();
        for (String fileCode : getFileCodes()) {
            try {
                Environment environment = mapper.readValue(new File("data/env." + fileCode + ".yaml"), Environment.class);
                environments.put(environment.id, environment);
            }
            catch (IOException e) {
                logger.error("", e);
            }
        }
        return environments;
    }

    @Override
    public List<Environment> getEnvironments() {
        return new ArrayList<>(getAllEnvironments().values());
    }

    @Override
    public Environment getEnvironment(String env) {
        return getAllEnvironments().get(env);
    }

}
