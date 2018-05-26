package org.neo.gomina.persistence.sonar

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Provider
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.sonar.SonarConfig
import java.io.File

// FIXME Reloadable

class SonarConfigProvider : Provider<SonarConfig> {
    private val mapper = ObjectMapper(YAMLFactory())
            .registerModule(KotlinModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    override fun get(): SonarConfig {
        val configFile = File("data/sonar.config.yaml")
        val config = mapper.readValue<SonarConfig>(configFile)
        return config.also { logger.info("Sonar repos config $config") }
    }

    companion object {
        private val logger = LogManager.getLogger(SonarConfigProvider::class.java)
    }
}
