package org.neo.gomina.persistence.model

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.dependency.*
import org.neo.gomina.model.dependency.Function
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import java.io.File

class InteractionsFileProvider : InteractionsProvider, AbstractFileRepo() {

    companion object {
        private val logger = LogManager.getLogger(InteractionsFileProvider.javaClass)
    }

    @Inject @Named("interactions.file") private lateinit var file: File
    @Inject private lateinit var providers: InteractionProviders

    @Inject fun init() {
        providers.providers.add(this)
    }
    
    fun read(file: File): List<Interactions> {
        return when (file.extension) {
            "yaml" -> yamlMapper.readValue(file)
            "json" -> jsonMapper.readValue(file)
            else -> throw IllegalArgumentException("Format not supported for $file, please use .yaml .json")
        }
    }

    override fun getAll(): List<Interactions> {
        return read(file)
    }
}

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
        logger.info("Hosts Database connected $host $port")
    }

    @Inject lateinit var enrichDependencies: EnrichDependencies // FIXME Get out, to higher level

    override fun getAll(): List<Interactions> {
        //val all = providers.flatMap { it.getAll() }
        val all = doGetAll()
        val enriched = enrichDependencies.enrich(all)
        return (all + enriched).merge().toList()
    }

    override fun getFor(serviceId: String): Interactions {
        return getAll().merge().find { it.serviceId == serviceId } ?: Interactions(serviceId)
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

    private fun doGetFor(serviceId: String): Interactions {
        pool.resource.use { jedis ->
            return jedis.keys("api:*:$serviceId:*").let {
                load(it, serviceId, jedis)
            }
        }
    }

    private fun load(keys: Collection<String>, service: String, jedis:Jedis): Interactions {
        val interactions = keys.map { key: String ->
            val type = key.split(':')[3]
            val data = jedis.hgetAll(key)
            when (type) {
                "exposes" -> data.toFunction()
                "uses" -> data.toFunctionUsage()
                else -> null
            }
        }
        return Interactions(service,
                interactions.filterIsInstance<Function>(),
                interactions.filterIsInstance<FunctionUsage>())
    }

    private fun Map<String, String>.toFunction()
            = this["name"]?.let { Function(it, this["type"] ?: "unknown") }
    private fun Map<String, String>.toFunctionUsage()
            = this["name"]?.let { FunctionUsage(it, this["type"] ?: "unknown", this["usage"]?.let { Usage(it) }) }
}


