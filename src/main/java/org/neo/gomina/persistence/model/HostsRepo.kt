package org.neo.gomina.persistence.model

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.host.Host
import org.neo.gomina.model.host.Hosts
import redis.clients.jedis.JedisPool
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

class RedisHosts : Hosts {

    companion object {
        private val logger = LogManager.getLogger(javaClass)
    }

    private lateinit var pool: JedisPool

    @Inject
    private fun initialize(@Named("database.host") host: String, @Named("database.port") port: Int) {
        pool = JedisPool(
                GenericObjectPoolConfig().apply { testOnBorrow = true },
                host, port, 10000, null, 4)
        logger.info("Hosts Database connected $host $port")
    }


    override fun getHosts(): List<Host> {
        pool.resource.use { jedis ->
            return jedis.keys("host:*").mapNotNull {
                jedis.hgetAll(it).toHost(it.substring(it.lastIndexOf(':') + 1))
            }
        }
    }

    override fun getHost(host: String): Host? {
        pool.resource.use { jedis ->
            return jedis.hgetAll("host:$host").toHost(host)
        }
    }

    private fun Map<String, String>.toHost(host: String): Host {
        return Host(
                host = host,
                dataCenter = this["data_center"],
                group = this["group"],
                type = this["type"] ?: "UNKNOWN",
                tags = this["tags"].toList(),
                username = this["username"],
                passwordAlias = this["password_alias"],
                proxyHost = this["proxy_host"],
                proxyUser = this["proxy_user"],
                sudo = this["sudo"]
        )
    }
}