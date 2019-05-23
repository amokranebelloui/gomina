package org.neo.gomina.persistence.model

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.inventory.*
import org.neo.gomina.model.monitoring.asInt
import org.neo.gomina.model.version.Version
import redis.clients.jedis.JedisPool
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RedisInventoryRepo : Inventory {

    companion object {
        private val logger = LogManager.getLogger(javaClass)
    }

    private lateinit var pool: JedisPool

    @Inject
    private fun initialize(@Named("database.host") host: String, @Named("database.port") port: Int) {
        pool = JedisPool(
                GenericObjectPoolConfig().apply { testOnBorrow = true },
                host, port, 10000, null, 3)
        logger.info("Inventory Database connected $host $port")
    }

    override fun getEnvironments(): Collection<Environment> {
        pool.resource.use { jedis ->
            return jedis.keys("env:*").mapNotNull { getEnvironment(it.substring(it.lastIndexOf(':') + 1)) }
        }
    }

    override fun getProdEnvironments(): Collection<Environment> {
        pool.resource.use { jedis ->
            return jedis.keys("env:*").mapNotNull { getEnvironment(it.substring(it.lastIndexOf(':') + 1)) }
                    .filter { it.type == "PROD" }
        }
    }

    override fun getEnvironment(env: String): Environment? {
        pool.resource.use { jedis ->
            //val services = jedis.keys("service:$env:*")
            val services = jedis.zrange("services:$env", 0, -1).map { svc ->
                val instances = jedis.keys("instance:$env:$svc:*").map {
                    val id = it.substring(it.lastIndexOf(':') + 1)
                    jedis.hgetAll(it).asInstance(id)
                }
                jedis.hgetAll("service:$env:$svc").asService(svc, instances)
            }
            return jedis.hgetAll("env:$env").asEnv(env, services)
        }
    }

    override fun addEnvironment(id: String, type: String, description: String?, monitoringUrl: String?) {
        pool.resource.use { jedis ->
            val envKey = "env:$id"
            if (jedis.exists(envKey)) {
                throw Exception("$id already exists")
            }
            jedis.hmset(envKey, listOfNotNull(
                    "creation_time" to LocalDateTime.now(Clock.systemUTC()).format(DateTimeFormatter.ISO_DATE_TIME),
                    type.let { "type" to it },
                    description?.let { "description" to it },
                    monitoringUrl?.let { "monitoring_url" to it }
            ).toMap())
        }
    }

    override fun updateEnvironment(id: String, type: String, description: String?, monitoringUrl: String?) {
        pool.resource.use { jedis ->
            jedis.hmset("env:$id", listOfNotNull(
                    "creation_time" to LocalDateTime.now(Clock.systemUTC()).format(DateTimeFormatter.ISO_DATE_TIME),
                    type.let { "type" to it },
                    description?.let { "description" to it },
                    monitoringUrl?.let { "monitoring_url" to it }
            ).toMap())
        }
    }

    override fun enableEnvironment(envId: String) {
        pool.resource.use { jedis ->
            jedis.hset("env:$envId", "disabled", false.toString())
        }
    }

    override fun disableEnvironment(envId: String) {
        pool.resource.use { jedis ->
            jedis.hset("env:$envId", "disabled", true.toString())
        }
    }

    override fun deleteEnvironment(id: String) {
        pool.resource.use { jedis ->
            jedis.del("env:$id")
        }
    }

    override fun addService(env: String, svc: String, type: String?, mode: ServiceMode?, activeCount: Int?, componentId: String?) {
        pool.resource.use { jedis ->
            val serviceKey = "service:$env:$svc"
            if (jedis.exists(serviceKey)) {
                throw Exception("$svc already exists for $env")
            }
            jedis.hmset(serviceKey, listOfNotNull(
                    "creation_time" to LocalDateTime.now(Clock.systemUTC()).format(DateTimeFormatter.ISO_DATE_TIME),
                    type?.let { "type" to it },
                    mode?.let { "mode" to it.name },
                    activeCount?.let { "active_count" to it.toString() },
                    componentId?.let { "component" to it }
            ).toMap())
            val servicesKey = "services:$env"
            val max = jedis.zrevrangeWithScores(servicesKey, 0, 0).firstOrNull()?.score ?: 0.0
            jedis.zadd(servicesKey, max + 1, svc)
        }
    }

    override fun updateService(env: String, svc: String, type: String?, mode: ServiceMode?, activeCount: Int?, componentId: String?) {
        pool.resource.use { jedis ->
            val serviceKey = "service:$env:$svc"
            if (!jedis.exists(serviceKey)) {
                addService(env, svc, type, mode, activeCount, componentId)
            }
            else {
                jedis.persist(serviceKey, mapOf(
                        "type" to type,
                        "mode" to mode?.name,
                        "active_count" to activeCount.toString(),
                        "component" to componentId
                ))
            }
        }
    }

    override fun renameService(env: String, svc: String, newSvc: String) {
        pool.resource.use { jedis ->
            val servicesKey = "services:$env"
            val score = jedis.zscore(servicesKey, svc)
            jedis.zrem(servicesKey, svc)
            jedis.zadd(servicesKey, score, newSvc)
            jedis.rename("service:$env:$svc", "service:$env:$newSvc")
            jedis.keys("instance:$env:$svc:*").forEach {
                jedis.rename(it, it.replace("instance:$env:$svc:", "instance:$env:$newSvc:"))
            }
        }
    }

    override fun reorderService(env: String, svc: String, target: String) {
        pool.resource.use { jedis ->
            val servicesKey = "services:$env"
            val from = jedis.zscore(servicesKey, svc)
            val to = jedis.zscore(servicesKey, target)
            if (from > to) {
                jedis.zrangeByScoreWithScores(servicesKey, "$to", "($from").forEach {
                    jedis.zadd(servicesKey, it.score + 1.0, it.element)
                }
                jedis.zadd(servicesKey, to, svc)
            }
            else if (from < to) {
                jedis.zrangeByScoreWithScores(servicesKey, "($from", "$to").forEach {
                    jedis.zadd(servicesKey, it.score - 1.0, it.element)
                }
                jedis.zadd(servicesKey, to, svc)
            }
            true
        }
    }

    override fun deleteService(env: String, svc: String) {
        pool.resource.use { jedis ->
            jedis.del("service:$env:$svc")
            jedis.zrem("services:$env", svc)
            jedis.keys("instance:$env:$svc:*").forEach {
                jedis.del(it)
            }
        }
    }

    override fun addInstance(env: String, svc: String, instanceId: String, host: String?, folder: String?) {
        pool.resource.use { jedis ->
            if (jedis.exists("instance:$env:$svc:$instanceId")) {
                throw Exception("$instanceId already exists for $env/$svc")
            }
            jedis.hmset("instance:$env:$svc:$instanceId", listOfNotNull(
                    "creation_time" to LocalDateTime.now(Clock.systemUTC()).format(DateTimeFormatter.ISO_DATE_TIME),
                    host?.let { "host" to it },
                    folder?.let { "folder" to it }
            ).toMap())
        }
    }

    override fun deleteInstance(env: String, svc: String, instanceId: String) {
        pool.resource.use { jedis ->
            jedis.del("instance:$env:$svc:$instanceId")
        }
    }

    override fun updateDeployedRevision(env: String, svc: String, instanceId: String, version: Version?) {
        pool.resource.use { jedis ->
            jedis.hmset("instance:$env:$svc:$instanceId", listOfNotNull(
                    "deployed_version_update_time" to LocalDateTime.now(Clock.systemUTC()).format(DateTimeFormatter.ISO_DATE_TIME),
                    version?.version?.let { "deployed_version" to it },
                    version?.revision?.let { "deployed_revision" to it }
            ).toMap())
        }
    }

    override fun updateConfigStatus(env: String, svc: String, instanceId: String, confRevision: String?, confCommitted: Boolean?, confUpToDate: Boolean?) {
        pool.resource.use { jedis ->
            jedis.hmset("instance:$env:$svc:$instanceId", listOfNotNull(
                    "config_status_update_time" to LocalDateTime.now(Clock.systemUTC()).format(DateTimeFormatter.ISO_DATE_TIME),
                    confRevision?.let { "conf_revision" to it },
                    confCommitted?.let { "conf_committed" to it.toString() },
                    confUpToDate?.let { "conf_up_to_date" to it.toString() }
            ).toMap())
        }
    }
}

private fun Map<String, String>.asEnv(id: String, services: List<Service>) =
        Environment(id,
                this["type"] ?: "UNKNOWN",
                this["description"],
                this["monitoring_url"],
                this["disabled"]?.toBoolean() != true,
                services
        )

private fun Map<String, String>.asService(id: String, instances: List<Instance>) =
        Service(id,
                this["type"],
                this["mode"]?.let { ServiceMode.valueOf(it) },
                this["active_count"].asInt,
                this["component"],
                instances,
                undefined = false
        )

private fun Map<String, String>.asInstance(id: String) =
        Instance(id,
                this["host"],
                this["folder"],

                Version.from(this["deployed_version"], this["deployed_revision"]),
                this["conf_revision"],
                this["conf_committed"]?.toBoolean(),
                this["conf_up_to_date"]?.toBoolean()
        )
