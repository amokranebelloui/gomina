package org.neo.gomina.persistence.model

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.component.Component
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.component.Scm
import redis.clients.jedis.Jedis
import java.io.File

class ComponentRepoFile : ComponentRepo, AbstractFileRepo() {
    companion object {
        private val logger = LogManager.getLogger(ComponentRepoFile.javaClass)
    }

    @Inject @Named("components.file")
    private lateinit var file: File

    fun read(file: File): List<Component> {
        return when (file.extension) {
            "yaml" -> yamlMapper.readValue(file)
            "json" -> jsonMapper.readValue(file)
            else -> throw IllegalArgumentException("Format not supported for $file, please use .yaml .json")
        }
    }

    override fun getAll(): List<Component> = read(file)

    override fun get(componentId: String): Component? = read(file).find { it.id == componentId }

    override fun disable(componentId: String) {
        TODO("not implemented")
    }

    override fun enable(componentId: String) {
        TODO("not implemented")
    }

}

class ComponentRepoRedis : ComponentRepo {

    companion object {
        private val logger = LogManager.getLogger(ComponentRepoRedis.javaClass)
    }

    private lateinit var jedis: Jedis

    @Inject
    private fun initialize(@Named("database.host") host: String, @Named("database.port") port: Int) {
        jedis = Jedis(host, port).also { it.select(1) }
        logger.info("Components Database connected $host $port")
    }

    override fun getAll(): List<Component> {
        val pipe = jedis.pipelined()
        val data = jedis.keys("component:*").map { it.substring(10) to pipe.hgetAll(it) }
        pipe.sync()
        return data.map { (id, data) -> toComponent(id, data.get()) }
    }

    override fun get(componentId: String): Component? {
        return jedis.hgetAll("component:$componentId")
                ?.let { toComponent(componentId, it) }
    }

    private fun toComponent(id: String, map: Map<String, String>) = Component(
            id = id,
            label = map["label"],
            type = map["type"],
            systems = map["systems"]?.split(",")?.map { it.trim() } ?: emptyList(),
            languages = map["languages"]?.split(",")?.map { it.trim() } ?: emptyList(),
            tags = map["tags"]?.split(",")?.map { it.trim() } ?: emptyList(),
            scm = Scm(
                    type = map["scm_type"] ?: "",
                    url = map["scm_url"] ?: "",
                    path = map["scm_path"] ?: "",
                    username = map["scm_username"] ?: "",
                    passwordAlias = map["scm_password_alias"] ?: ""
            ) ,
            maven = map["maven"],
            sonarServer = map["sonarServer"] ?: "",
            jenkinsServer = map["jenkinsServer"] ?: "",
            jenkinsJob = map["jenkinsJob"],
            disabled = map["disabled"]?.toBoolean() == true
    )

    override fun disable(componentId: String) {
        "component:$componentId".let { key ->
            if (jedis.exists(key)) jedis.hset(key, "disabled", "true")
        }
    }

    override fun enable(componentId: String) {
        "component:$componentId".let { key ->
            if (jedis.exists(key)) jedis.hset(key, "disabled", "false")
        }
    }

}