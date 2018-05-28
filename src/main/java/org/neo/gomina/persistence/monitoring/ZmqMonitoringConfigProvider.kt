package org.neo.gomina.persistence.monitoring

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Provider
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.zmqmonitoring.ZmqMonitorConfig
import java.io.File

// FIXME Reloadable

class ZmqMonitoringConfigProvider : Provider<ZmqMonitorConfig> {
    private val mapper = ObjectMapper(YAMLFactory())
            .registerModule(KotlinModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    override fun get(): ZmqMonitorConfig {
        val configFile = File("data/monitoring.zmq.config.yaml")
        val config = mapper.readValue<ZmqMonitorConfig>(configFile)
        return config.also { logger.info("Monitoring repos config $config") }
    }

    companion object {
        private val logger = LogManager.getLogger(ZmqMonitoringConfigProvider::class.java)
    }
}
