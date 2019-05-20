package org.neo.gomina.plugins

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.neo.gomina.model.inventory.Environment
import org.neo.gomina.model.inventory.Instance
import org.neo.gomina.model.inventory.Service
import org.neo.gomina.module.config.ConfigLoader
import org.neo.gomina.persistence.model.persist
import org.neo.gomina.persistence.model.toStr
import redis.clients.jedis.Jedis
import java.io.File

val jsonMapper = ObjectMapper(JsonFactory())
        .registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
        .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)


private val mapper = ObjectMapper(YAMLFactory())
        .registerModule(KotlinModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

fun main(args: Array<String>) {
    val config = ConfigLoader().load()

    val jedis = Jedis(config.database.host, config.database.port)
    jedis.select(1)
    val file = "C:\\Work\\Code\\Tools\\governance-tools\\data\\projects.json"
    jsonMapper.readValue<List<DComponent>>(File(file)).forEach {
        //println(it)
        val systems = when {
            it.id.startsWith("tradex-") -> listOf("tradex")
            it.id.startsWith("postx-") -> listOf("postx")
            it.id.startsWith("lib-") -> listOf("libraries")
            else -> listOf("misc")
        }
        if (jedis.exists("component:${it.id}")) {
            println("${it.id} already exists")
        }
        else {
            jedis.persist("component:${it.id}", mapOf(
                    "label" to it.id,
                    "type" to it.type,
                    "systems" to systems.toStr(),
                    "languages" to listOf("java").toStr(),
                    "scm_type" to "svn",
                    "scm_url" to "http://svn/actions/venteaction/vaction/Developpement",
                    "scm_path" to it.svnUrl,
                    "scm_username" to "belloui_a",
                    "scm_pasword_alias" to "@belloui_a",
                    "sonar_server" to "vaction",
                    "jenkins_server" to "vaction",
                    "jenkins_job" to it.jenkinsJob
            ))
            println("Migrated ${it.id}")
        }
    }
}


fun main1(args: Array<String>) {
    val file = "env.registry-prod.json"
    val old:DEnvironment = jsonMapper.readValue(File("data/oldx/$file"))
    val new = map(old)
    jsonMapper.writeValue(File("data/$file"), new)

}

private fun map(old: DEnvironment): Environment {
    val services = old.instances
            .groupBy { i -> i.svc }
            .map { (svc, instances) -> Triple(svc, instances.first(), instances) }
            .map { (svc, i, instances) ->
                Service(svc = svc,
                        type = i.type,
                        componentId = i.project,
                        instances = instances.map { Instance(it.id, it.host, it.folder) },
                        undefined = false
                )
            }

    return Environment(old.code, old.name, old.type, old.monitoringUrl, old.active, services)
}


private data class DEnvironment (
        val name: String,
        val code: String,
        val type: String = "UNKNOWN",
        val monitoringUrl: String?,
        val active: Boolean = false,
        val instances: List<DInstance> = emptyList()
)

private data class DInstance (
        val id: String,
        val type: String?,
        val svc: String,
        val host: String?,
        val folder: String?,
        val project: String?
)

private data class DComponent (
        val id: String,
        val type: String?,
        val svnUrl: String?,
        val maven: String?,
        val jenkinsJob: String?
)
