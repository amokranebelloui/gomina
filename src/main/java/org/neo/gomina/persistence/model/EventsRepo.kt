package org.neo.gomina.persistence.model

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.event.Event
import org.neo.gomina.model.event.Events
import redis.clients.jedis.JedisPool
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class RedisEvents : Events {

    companion object {
        private val logger = LogManager.getLogger(javaClass)
    }

    private lateinit var pool: JedisPool

    @Inject
    private fun initialize(@Named("database.host") host: String, @Named("database.port") port: Int) {
        pool = JedisPool(
                GenericObjectPoolConfig().apply { testOnBorrow = true },
                host, port, 10000, null, 6)
        logger.info("Events Database connected $host $port")
    }

    override fun all(): List<Event> {
        pool.resource.use { jedis ->
            return jedis.keys("event:*:*").mapNotNull {
                jedis.hgetAll(it).toEvent(it.substring(it.indexOf(':') + 1)?.let { it.substring(it.indexOf(':') + 1) })
            }
        }
    }

    override fun forEnv(envId: String): List<Event> {
        pool.resource.use { jedis ->
            val pipe = jedis.pipelined()
            val vals = (
                    jedis.zrange("events:env:$envId", 0, -1) +
                    jedis.zrange("events:global", 0, -1)
                    )
                    .map { it to pipe.hgetAll("event:$it") }
            pipe.sync()
            return vals.map { (idWithSource, data) -> data.get().toEvent(idWithSource.substring(idWithSource.indexOf(':') + 1)) }
        }
    }

    override fun forComponent(componentId: String): List<Event> {
        pool.resource.use { jedis ->
            val pipe = jedis.pipelined()
            val vals = (
                    jedis.zrange("events:component:$componentId", 0, -1) +
                    jedis.zrange("events:global", 0, -1)
                    )
                    .map { it to pipe.hgetAll("event:$it") }
            pipe.sync()
            return vals.map { (idWithSource, data) -> data.get().toEvent(idWithSource.substring(idWithSource.indexOf(':') + 1)) }
        }
    }

    override fun releases(componentId: String, prodEnvs: List<String>): List<Event> {
        return this.forComponent(componentId).filter { it.type == "release" && prodEnvs.contains(it.envId) }
    }

    private fun Map<String, String>.toEvent(id: String): Event {
        return Event(
                id = id,
                timestamp = this["timestamp"].let { LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) },
                type = this["type"],
                message = this["message"],
                envId = this["env"],
                instanceId = this["instance"],
                componentId = this["component"],
                version = this["version"]
        )
    }

    override fun save(events: List<Event>, group: String) {
        pool.resource.use { jedis ->
            val pipe = jedis.pipelined()
            events.forEach { event ->
                val eventId = event.id.replace(':', '-')
                pipe.hmset("event:$group:$eventId", listOfNotNull(
                        "timestamp" to event.timestamp.format(DateTimeFormatter.ISO_DATE_TIME),
                        event.type?.let { "type" to it },
                        event.message?.let { "message" to it },
                        event.envId?.let { "env" to it },
                        event.instanceId?.let { "instance" to it },
                        event.componentId?.let { "component" to it },
                        event.version?.let { "version" to it }
                ).toMap())

                val time = Date.from(event.timestamp.atZone(ZoneOffset.UTC).toInstant()).time.toDouble()
                val idWithSource = group + ":" + eventId
                event.envId?.let { pipe.zadd("events:env:$it", time, idWithSource) }
                event.instanceId?.let { pipe.zadd("events:instance:$it", time, idWithSource) }
                event.componentId?.let { pipe.zadd("events:component:$it", time, idWithSource) }
                if (event.global) { pipe.zadd("events:global", time, idWithSource) }
            }
            pipe.sync()
        }

    }
}