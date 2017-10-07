package org.neo.gomina.module.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class ConfigLoaderTest {

    private final static Logger logger = LogManager.getLogger(ConfigLoaderTest.class);

    @Test
    public void testLoadConfig() throws Exception {
        ConfigLoader configLoader = new ConfigLoader();
        Config config = configLoader.load();

        logger.info(config);
    }

}