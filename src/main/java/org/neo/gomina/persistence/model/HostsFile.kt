package org.neo.gomina.persistence.model

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.hosts.Host
import org.neo.gomina.model.hosts.Hosts
import java.io.File

class HostsFile : Hosts, AbstractFileRepo() {

    companion object {
        private val logger = LogManager.getLogger(HostsFile.javaClass)
    }

    @Inject @Named("hosts.file") private lateinit var file: File

    fun read(file: File): List<Host> {
        return when (file.extension) {
            "yaml" -> yamlMapper.readValue(file)
            "json" -> jsonMapper.readValue(file)
            else -> throw IllegalArgumentException("Format not supported for $file, please use .yaml .json")
        }
    }

    override fun getHosts(): List<Host> {
        return try {
            read(file)
        } catch (e: Exception) {
            logger.error("", e)
            emptyList()
        }
    }
    override fun getHost(host: String): Host? {
        return try {
            read(file).find { it.host == host }
        } catch (e: Exception) {
            null
        }
    }
}