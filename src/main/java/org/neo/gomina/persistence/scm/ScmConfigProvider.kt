package org.neo.gomina.persistence.scm

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Provider
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.scm.impl.ScmConfig
import java.io.File

// FIXME Reloadable

class ScmConfigProvider : Provider<ScmConfig> {
    private val mapper = ObjectMapper(YAMLFactory())
            .registerModule(KotlinModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    override fun get(): ScmConfig {
        val configFile = File("data/scm.config.yaml")
        val config = mapper.readValue<ScmConfig>(configFile)
        logger.info("SCM repos config $config")
        return config
    }

    companion object {
        private val logger = LogManager.getLogger(ScmConfigProvider::class.java)
    }
}

