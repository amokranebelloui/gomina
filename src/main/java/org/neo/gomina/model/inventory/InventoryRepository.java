package org.neo.gomina.model.inventory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;

public class InventoryRepository {

    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public InventoryRepository() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static void main(String[] args) {
        Environment env = new InventoryRepository().getEnvironment();
        System.out.println(env);
    }

    public Environment getEnvironment() {
        Environment env = null;
        try {
            env = mapper.readValue(new File("data/env-uat.yaml"), Environment.class);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return env;
    }

}
