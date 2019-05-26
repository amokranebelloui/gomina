package org.neo.gomina.module.config

import org.apache.logging.log4j.LogManager
import org.junit.Test
import java.io.File

class ConfigLoaderTest {

    @Test
    fun testLoadConfig() {
        val configLoader = ConfigLoader(File("config/config.yaml"))
        val config = configLoader.load()

        logger.info(config)
    }

    companion object {
        private val logger = LogManager.getLogger(ConfigLoaderTest::class.java)
    }

}