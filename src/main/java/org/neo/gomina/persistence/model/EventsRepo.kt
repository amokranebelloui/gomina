package org.neo.gomina.persistence.model

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.event.Event
import org.neo.gomina.model.event.EventCategory
import org.neo.gomina.model.event.Events
import redis.clients.jedis.JedisPool
import redis.clients.jedis.Pipeline
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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

    override fun forEnv(envId: String): List<Event> {
        pool.resource.use { jedis ->
            val year = LocalDateTime.now(ZoneOffset.UTC).minusYears(1).toScore()
            val day = LocalDateTime.of(LocalDate.now(ZoneOffset.UTC).minusDays(1), LocalTime.MIDNIGHT).toScore()
            val pipe = jedis.pipelined()
            return process(pipe,
                    jedis.zrevrangeByScore("events:env:version:$envId", "+inf", "($year") +
                    jedis.zrevrangeByScore("events:env:release:$envId", "+inf", "($year") +
                    jedis.zrevrangeByScore("events:env:runtime:$envId", "+inf", "($day")
                    //jedis.zrange("events:global", 0, -1)
            )
        }
    }

    override fun forComponent(componentId: String): List<Event> {
        pool.resource.use { jedis ->
            val year = LocalDateTime.now(ZoneOffset.UTC).minusYears(1).toScore()
            val day = LocalDateTime.of(LocalDate.now(ZoneOffset.UTC).minusDays(7), LocalTime.MIDNIGHT).toScore()
            val pipe = jedis.pipelined()
            return process(pipe,
                    jedis.zrevrangeByScore("events:component:version:$componentId", "+inf", "($year") +
                    jedis.zrevrangeByScore("events:component:release:$componentId", "+inf", "($year") +
                    jedis.zrevrangeByScore("events:component:runtime:$componentId", "+inf", "($day")
                    //jedis.zrange("events:component:$componentId", 0, -1) +
                    //jedis.zrange("events:global", 0, -1)
            )
        }
    }

    override fun releases(componentId: String, prodEnvs: List<String>): List<Event> {
        pool.resource.use { jedis ->
            val pipe = jedis.pipelined()
            return process(pipe,
                    jedis.zrange("events:component:release:$componentId", 0, -1)
            )
            .filter { prodEnvs.contains(it.envId) }
        }
    }

    private fun process(pipe: Pipeline, keys: Set<String>): List<Event> {
        val eventFutures = keys
                .map { it to pipe.hgetAll("event:$it") }
        pipe.sync()
        return eventFutures.map { (idWithSource, data) ->
            val split = idWithSource.split(':')
            data.get().toEvent(split[1], EventCategory.valueOf(split[0].toUpperCase()))
        }
    }

    private fun Map<String, String>.toEvent(id: String, group: EventCategory): Event {
        return Event(
                id = id,
                timestamp = this["timestamp"].let { LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) },
                group = group,
                type = this["type"],
                message = this["message"],
                envId = this["env"],
                instanceId = this["instance"],
                componentId = this["component"],
                version = this["version"]
        )
    }

    override fun save(events: List<Event>) {
        pool.resource.use { jedis ->
            val pipe = jedis.pipelined()
            events.forEach { event ->
                val eventId = event.id.replace(':', '-')
                val group = event.group.toString().toLowerCase()
                pipe.hmset("event:$group:$eventId", listOfNotNull(
                        "timestamp" to event.timestamp.format(DateTimeFormatter.ISO_DATE_TIME),
                        event.type?.let { "type" to it },
                        event.message?.let { "message" to it },
                        event.envId?.let { "env" to it },
                        event.instanceId?.let { "instance" to it },
                        event.componentId?.let { "component" to it },
                        event.version?.let { "version" to it }
                ).toMap())

                val time = event.timestamp.toScore()
                val idWithSource = "$group:$eventId"
                if (event.envId != null) {
                    pipe.zadd("events:env:${event.envId}", time, idWithSource)
                    pipe.zadd("events:env:$group:${event.envId}", time, idWithSource)
                }
                if (event.instanceId != null) {
                    pipe.zadd("events:instance:${event.instanceId}", time, idWithSource)
                    pipe.zadd("events:instance:$group:${event.instanceId}", time, idWithSource)
                }
                if (event.componentId != null) {
                    pipe.zadd("events:component:${event.componentId}", time, idWithSource)
                    pipe.zadd("events:component:$group:${event.componentId}", time, idWithSource)
                }
                if (event.global) {
                    pipe.zadd("events:global", time, idWithSource)
                }
            }
            pipe.sync()
        }

    }


}

fun main(args: Array<String>) {
    println(LocalDateTime.now(ZoneOffset.UTC).minusYears(1).toScore())
}