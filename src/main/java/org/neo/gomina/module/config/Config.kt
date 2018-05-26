package org.neo.gomina.module.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.neo.gomina.plugins.monitoring.zmq.ZmqMonitorConfig
import org.neo.gomina.plugins.ssh.SshConfig
import java.io.File

data class Config (

        var name: String? = null,
        var passwordsFile: String? = null,

        var inventory: Map<String, String>? = mapOf(),

        var ssh: SshConfig = SshConfig(),

        var zmqMonitoring: ZmqMonitorConfig = ZmqMonitorConfig()

)


class ConfigLoader {

    private val mapper = ObjectMapper(YAMLFactory())
            .registerModule(KotlinModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun load(): Config {
        val configFile = File("config/config.yaml")
        return mapper.readValue(configFile)
    }
}