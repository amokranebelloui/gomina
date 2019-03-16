package org.neo.gomina.persistence.model

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.component.Component
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.component.NewComponent
import org.neo.gomina.model.component.Scm
import redis.clients.jedis.JedisPool
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

    override fun add(component: NewComponent) {
        TODO("not implemented")
    }

    override fun addSystem(componentId: String, system: String) { TODO("not implemented") }
    override fun deleteSystem(componentId: String, system: String) { TODO("not implemented") }

    override fun addLanguage(componentId: String, language: String) { TODO("not implemented")}
    override fun deleteLanguage(componentId: String, language: String) { TODO("not implemented")}
    override fun addTag(componentId: String, tag: String) { TODO("not implemented")}
    override fun deleteTag(componentId: String, tag: String) { TODO("not implemented")}


    override fun disable(componentId: String) {
        TODO("not implemented")
    }

    override fun enable(componentId: String) {
        TODO("not implemented")
    }

}

class RedisComponentRepo : ComponentRepo {
    companion object {
        private val logger = LogManager.getLogger(RedisComponentRepo.javaClass)
    }

    private lateinit var pool: JedisPool

    @Inject
    private fun initialize(@Named("database.host") host: String, @Named("database.port") port: Int) {
        pool = JedisPool(
                GenericObjectPoolConfig().apply { testOnBorrow = true },
                host, port, 10000, null, 1)
        logger.info("Components Database connected $host $port")
    }

    override fun getAll(): List<Component> {
        pool.resource.use { jedis ->
            val pipe = jedis.pipelined()
            val data = jedis.keys("component:*").map { it.substring(10) to pipe.hgetAll(it) }
            pipe.sync()
            return data.map { (id, data) -> toComponent(id, data.get()) }
        }
    }

    override fun get(componentId: String): Component? {
        pool.resource.use { jedis ->
            return jedis.hgetAll("component:$componentId")
                    ?.let { toComponent(componentId, it) }
        }
    }

    private fun toComponent(id: String, map: Map<String, String>): Component {
        return Component(
                id = id,
                label = map["label"],
                type = map["type"],
                systems = map["systems"].toList(),
                languages = map["languages"].toList(),
                tags = map["tags"].toList(),
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
    }

    override fun add(component: NewComponent) {
        pool.resource.use { jedis ->
            if (jedis.exists("component:${component.id}")) {
                throw Exception("${component.id} already exists")
            }
            jedis.hmset("component:${component.id}", listOfNotNull(
                    "label" to component.label,
                    "type" to component.type,
                    "systems" to component.systems.toStr(),
                    "languages" to component.languages.toStr(),
                    "tags" to component.tags.toStr(),
                    "scm_type" to (component.scm?.type ?: ""),
                    "scm_url" to (component.scm?.url ?: ""),
                    "scm_path" to (component.scm?.path ?: ""),
                    component.sonarServer?.let { "sonarServer" to it },
                    component.jenkinsServer?.let { "jenkinsServer" to it },
                    component.jenkinsJob?. let { "jenkinsJob" to it }
            ).toMap())
        }
    }

    override fun addSystem(componentId: String, system: String) {
        pool.resource.use { jedis ->
            jedis.hget("component:$componentId", "systems").toList().toSet()
                    .plus(system)
                    .toStr().let { jedis.hset("component:$componentId", "systems", it) }
        }
    }

    override fun deleteSystem(componentId: String, system: String) {
        pool.resource.use { jedis ->
            jedis.hget("component:$componentId", "systems").toList().toSet()
                    .minus(system)
                    .toStr().let { jedis.hset("component:$componentId", "systems", it) }
        }
    }

    override fun addLanguage(componentId: String, language: String) {
        pool.resource.use { jedis ->
            jedis.hget("component:$componentId", "languages").toList().toSet()
                    .plus(language)
                    .toStr().let { jedis.hset("component:$componentId", "languages", it) }
        }
    }

    override fun deleteLanguage(componentId: String, language: String) {
        pool.resource.use { jedis ->
            jedis.hget("component:$componentId", "languages").toList().toSet()
                    .minus(language)
                    .toStr().let { jedis.hset("component:$componentId", "languages", it) }
        }
    }

    override fun addTag(componentId: String, tag: String) {
        pool.resource.use { jedis ->
            jedis.hget("component:$componentId", "tags").toList().toSet()
                    .plus(tag)
                    .toStr().let { jedis.hset("component:$componentId", "tags", it) }
        }
    }

    override fun deleteTag(componentId: String, tag: String) {
        pool.resource.use { jedis ->
            jedis.hget("component:$componentId", "tags").toList().toSet()
                    .minus(tag)
                    .toStr().let { jedis.hset("component:$componentId", "tags", it) }
        }
    }

    override fun disable(componentId: String) {
        pool.resource.use { jedis ->
            "component:$componentId".let { key ->
                if (jedis.exists(key)) jedis.hset(key, "disabled", "true")
            }
        }
    }

    override fun enable(componentId: String) {
        pool.resource.use { jedis ->
            "component:$componentId".let { key ->
                if (jedis.exists(key)) jedis.hset(key, "disabled", "false")
            }
        }
    }

    private fun String?.toList() = this?.split(",")?.map { it.trim() } ?: emptyList()

    private fun Collection<String>.toStr() = this.joinToString(separator = ",")

}