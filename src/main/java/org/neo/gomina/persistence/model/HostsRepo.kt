package org.neo.gomina.persistence.model

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.host.Host
import org.neo.gomina.model.host.Hosts
import redis.clients.jedis.JedisPool
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
                osFamily = this["os_family"],
                os = this["os"],
                tags = this["tags"].toList(),
                username = this["username"],
                passwordAlias = this["password_alias"],
                proxyHost = this["proxy_host"],
                proxyUser = this["proxy_user"],
                sudo = this["sudo"],
                unexpectedFolders = this["unexpected_folders"].toList()
        )
    }

    override fun addHost(hostId: String) {
        pool.resource.use { jedis ->
            val envKey = "host:$hostId"
            if (jedis.exists(envKey)) {
                throw Exception("$hostId already exists")
            }
            jedis.hmset(envKey, listOfNotNull(
                    "creation_time" to LocalDateTime.now(Clock.systemUTC()).format(DateTimeFormatter.ISO_DATE_TIME)
            ).toMap())
        }
    }

    override fun updateHost(hostId: String, dataCenter: String?, group: String?, type: String, osFamily: String?, os: String?, tags: List<String>) {
        pool.resource.use { jedis ->
            jedis.persist("host:$hostId", mapOf(
                    "update_time" to LocalDateTime.now(Clock.systemUTC()).format(DateTimeFormatter.ISO_DATE_TIME),
                    dataCenter.let { "data_center" to it },
                    "group" to group,
                    "type" to type,
                    "os_family" to osFamily,
                    "os" to os,
                    "tags" to tags.toStr()
            ))
        }
    }

    override fun updateConnectivity(hostId: String, username: String?, passwordAlias: String?, proxyHost: String?, proxyUser: String?, sudo: String?) {
        pool.resource.use { jedis ->
            jedis.hmset("host:$hostId", listOfNotNull(
                    "update_time" to LocalDateTime.now(Clock.systemUTC()).format(DateTimeFormatter.ISO_DATE_TIME),
                    username.let { "username" to it },
                    passwordAlias?.let { "password_alias" to it },
                    proxyHost.let { "proxy_host" to it },
                    proxyUser.let { "proxy_user" to it },
                    sudo.let { "sudo" to it }
            ).toMap())
        }
    }

    override fun updateUnexpectedFolders(host: String, unexpectedFolders: List<String>) {
        pool.resource.use { jedis ->
            jedis.hmset("host:$host", listOfNotNull(
                    "ssh_update_time" to LocalDateTime.now(Clock.systemUTC()).format(DateTimeFormatter.ISO_DATE_TIME),
                    unexpectedFolders.let { "unexpected_folders" to it.toStr() }
            ).toMap())
        }
    }
}