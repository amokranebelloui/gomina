package org.neo.gomina.module.config

import org.apache.logging.log4j.LogManager
import org.junit.Test

class ConfigLoaderTest {

    @Test
    fun testLoadConfig() {
        val configLoader = ConfigLoader()
        val config = configLoader.load()

        logger.info(config)
    }

    companion object {
        private val logger = LogManager.getLogger(ConfigLoaderTest::class.java)
    }

}