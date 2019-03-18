package org.neo.gomina.tools

import redis.clients.jedis.Jedis

private val host: String = "localhost"
private val port: Int = 7070
private val jedis = Jedis(host, port)

fun main(args: Array<String>) {
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