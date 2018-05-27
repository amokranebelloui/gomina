package org.neo.gomina.persistence.ssh

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Provider
import org.apache.logging.log4j.LogManager
import org.neo.gomina.plugins.ssh.SshConfig
import java.io.File

// FIXME Reloadable

class SshConfigProvider : Provider<SshConfig> {
    private val mapper = ObjectMapper(YAMLFactory())
            .registerModule(KotlinModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    override fun get(): SshConfig {
        val configFile = File("data/ssh.config.yaml")
        val config = mapper.readValue<SshConfig>(configFile)
        return config.also { logger.info("SSH repos config $config") }
    }

    companion object {
        private val logger = LogManager.getLogger(SshConfigProvider::class.java)
    }
}
