package org.neo.gomina.persistence.jenkins

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Provider
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.jenkins.JenkinsConfig
import java.io.File

// FIXME Reloadable

class JenkinsConfigProvider : Provider<JenkinsConfig> {
    private val mapper = ObjectMapper(YAMLFactory())
            .registerModule(KotlinModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    override fun get(): JenkinsConfig {
        val configFile = File("data/jenkins.config.yaml")
        val config = mapper.readValue<JenkinsConfig>(configFile)
        return config.also { logger.info("Jenkins repos config $config") }
    }

    companion object {
        private val logger = LogManager.getLogger(JenkinsConfigProvider::class.java)
    }
}
