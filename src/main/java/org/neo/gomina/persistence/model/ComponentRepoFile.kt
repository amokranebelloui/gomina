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

}

class ComponentRepoRedis : ComponentRepo {

    companion object {
        private val logger = LogManager.getLogger(ComponentRepoRedis.javaClass)
    }

    private val jedis = Jedis("localhost", 6379).also { it.select(1) }

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
            jenkinsJob = map["jenkinsJob"]
    )
}