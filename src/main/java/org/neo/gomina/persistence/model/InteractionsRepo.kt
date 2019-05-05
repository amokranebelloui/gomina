package org.neo.gomina.persistence.model

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.dependency.*
import org.neo.gomina.model.dependency.Function
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool

class RedisInteractionsRepository : InteractionsRepository {

    companion object {
        private val logger = LogManager.getLogger(javaClass)
    }

    private lateinit var pool: JedisPool

    @Inject
    private fun initialize(@Named("database.host") host: String, @Named("database.port") port: Int) {
        pool = JedisPool(
                GenericObjectPoolConfig().apply { testOnBorrow = true },
                host, port, 10000, null, 2)
        logger.info("Interactions Database connected $host $port")
    }

    @Inject lateinit var enrichDependencies: EnrichDependencies // FIXME Get out, to higher level

    private val manualSource = "manual"

    override fun getAll(): List<Interactions> {
        val all = doGetAll()
        val enriched = enrichDependencies.enrich(all)
        return (all + enriched).merge().toList()
    }

    private fun doGetAll(): List<Interactions> {
        pool.resource.use { jedis ->
            return jedis.keys("api:*").map { key ->
                val service = key.split(':')[2]
                service to key
            }
            .groupBy({(service, key) -> service}) { (service, key) -> key }
            .map { (service, key) ->
                load(key, service, jedis)
            }
        }
    }

    private fun load(keys: Collection<String>, service: String, jedis:Jedis): Interactions {
        val interactions = keys.map { key: String ->
            val source = key.split(':')[1]
            val type = key.split(':')[3]
            val data = jedis.hgetAll(key)
            when (type) {
                "exposes" -> data.toFunction(listOf(source))
                "uses" -> data.toFunctionUsage(listOf(source))
                else -> null
            }
        }
        return Interactions(service,
                interactions.filterIsInstance<Function>(),
                interactions.filterIsInstance<FunctionUsage>())
    }

    private fun Map<String, String>.toFunction(sources: List<String>): Function? {
        return this["name"]?.let {
            Function(it,
                    this["type"] ?: "unknown",
                    sources
            )
        }
    }
    private fun Map<String, String>.toFunctionUsage(sources: List<String>): FunctionUsage? {
        return this["name"]?.let {
            FunctionUsage(it,
                    this["type"] ?: "unknown",
                    this["usage"]?.let { Usage(it) },
                    sources
            )
        }
    }

    override fun update(source: String, interactions: List<Interactions>) {
        pool.resource.use { jedis ->
            val pipe = jedis.pipelined()
            jedis.keys("api:$source:*").forEach { pipe.del(it) }
            pipe.sync()

            interactions.forEach { service ->
                service.exposed.forEach {
                    jedis.hmset("api:$source:${service.serviceId}:exposes:${it.name}", mapOf(
                            "name" to it.name,
                            "type" to it.type
                    ))
                }
                service.used.forEach {
                    jedis.hmset("api:$source:${service.serviceId}:uses:${it.function.name}", listOfNotNull(
                            "name" to it.function.name,
                            "type" to it.function.type,
                            it.usage?.usage?.let { "usage" to it.toString() }
                    ).toMap())
                }

            }
        }
    }

    override fun getApi(componentId: String): List<Function> {
        pool.resource.use { jedis ->
            return jedis.keys("api:*:$componentId:exposes:*")
                    .mapNotNull { key ->
                        val source = key.split(':')[1]
                        jedis.hgetAll(key).toFunction(listOf(source))
                    }
                    .mergeFunctions()
        }
    }

    override fun addApi(componentId: String, function: Function) {
        pool.resource.use { jedis ->
            jedis.hmset("api:$manualSource:$componentId:exposes:${function.name}", mapOf(
                    "name" to function.name,
                    "type" to function.type
            ))
        }
    }

    override fun removeApi(componentId: String, name: String) {
        pool.resource.use { jedis ->
            jedis.del("api:$manualSource:$componentId:exposes:$name")
        }
    }

    override fun getUsage(componentId: String): List<FunctionUsage> {
        pool.resource.use { jedis ->
            return jedis.keys("api:*:$componentId:uses:*")
                    .mapNotNull { key ->
                        val source = key.split(':')[1]
                        jedis.hgetAll(key).toFunctionUsage(listOf(source))
                    }
                    .mergeFunctionUsage()
        }
    }

    override fun addUsage(componentId: String, functionUsage: FunctionUsage) {
        pool.resource.use { jedis ->
            jedis.hmset("api:$manualSource:$componentId:uses:${functionUsage.function.name}", listOfNotNull(
                    "name" to functionUsage.function.name,
                    "type" to functionUsage.function.type,
                    functionUsage.usage?.usage?.let { "usage" to it.toString() }
            ).toMap())
        }
    }

    override fun removeUsage(componentId: String, name: String) {
        pool.resource.use { jedis ->
            jedis.del("api:$manualSource:$componentId:uses:$name")
        }
    }
}


