package org.neo.gomina.tools

import org.neo.gomina.module.config.ConfigLoader
import redis.clients.jedis.Jedis
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*


fun main(args: Array<String>) {
    val config = ConfigLoader(File("config/config.exane.yaml")).load()
    val jedis = Jedis(config.database.host, config.database.port)
    jedis.select(6)
    val pipe = jedis.pipelined()

    val dataRaw = jedis.keys("event:*").map {
        it to pipe.hmget(it, "timestamp", "type", "env", "instance", "component")
    }
    pipe.sync()
    dataRaw.forEach { (key, dataF) ->
        val idWithSource = key.replace(Regex("^event:"), "")
        val group = idWithSource.split(':')[0]
        println("$idWithSource $group")
        val data = dataF.get()
        val timestamp = data[0].let { LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) }
        val time = Date.from(timestamp.atZone(ZoneOffset.UTC).toInstant()).time.toDouble()
        val type = data[1]
        val envId = data[2]
        val instanceId = data[3]
        val componentId = data[4]

        pipe.zadd("events:env:$group:$envId", time, idWithSource)
        pipe.zadd("events:env:$envId", time, idWithSource)
        pipe.zadd("events:instance:$group:$instanceId", time, idWithSource)
        pipe.zadd("events:instance:$instanceId", time, idWithSource)
        pipe.zadd("events:component:$group:$componentId", time, idWithSource)
        pipe.zadd("events:component:$componentId", time, idWithSource)
    }
    pipe.sync()
}

private fun refactorComponentFields(jedis: Jedis) {
    jedis.select(1)
    jedis.keys("component:*").forEach { k ->
        jedis.hget(k, "sonarServer")?.let { jedis.hset(k, "sonar_server", it) }
        jedis.hget(k, "jenkinsServer")?.let { jedis.hset(k, "jenkins_server", it) }
        jedis.hget(k, "jenkinsJob")?.let { jedis.hset(k, "jenkins_job", it) }

        jedis.hdel(k, "sonarServer")
        jedis.hdel(k, "jenkinsServer")
        jedis.hdel(k, "jenkinsJob")
    }
}