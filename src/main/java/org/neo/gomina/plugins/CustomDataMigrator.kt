package org.neo.gomina.plugins

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
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
    //val components = File("C:\\Work\\Code\\Tools\\governance-tools\\data\\projects.json")

    reloadEnvironment(File("C:\\Work\\Code\\Tools\\governance-tools\\data\\env.monitorx-prod.json"))
    reloadEnvironment(File("C:\\Work\\Code\\Tools\\governance-tools\\data\\env.monitorx-stg.json"))
    reloadEnvironment(File("C:\\Work\\Code\\Tools\\governance-tools\\data\\env.postx-int.json"))
    reloadEnvironment(File("C:\\Work\\Code\\Tools\\governance-tools\\data\\env.postx-prod.json"))
    reloadEnvironment(File("C:\\Work\\Code\\Tools\\governance-tools\\data\\env.registry-prod.json"))
    reloadEnvironment(File("C:\\Work\\Code\\Tools\\governance-tools\\data\\env.registry-stg.json"))
    //reloadEnvironment(File("C:\\Work\\Code\\Tools\\governance-tools\\data\\env.tradex-hom.json"))
    //reloadEnvironment(File("C:\\Work\\Code\\Tools\\governance-tools\\data\\env.tradex-int.json"))
    //reloadEnvironment(File("C:\\Work\\Code\\Tools\\governance-tools\\data\\env.tradex-preprod.json"))
    reloadEnvironment(File("C:\\Work\\Code\\Tools\\governance-tools\\data\\env.tradex-prod-est.json"))
    reloadEnvironment(File("C:\\Work\\Code\\Tools\\governance-tools\\data\\env.tradex-prod-pt.json"))
    reloadEnvironment(File("C:\\Work\\Code\\Tools\\governance-tools\\data\\env.tradex-stg.json"))
    reloadEnvironment(File("C:\\Work\\Code\\Tools\\governance-tools\\data\\env.tradex-uat.json"))
    reloadEnvironment(File("C:\\Work\\Code\\Tools\\governance-tools\\data\\env.tradex-uatb.json"))

}

private fun reloadEnvironment(file: File) {
    val config = ConfigLoader(File("config/config.yaml")).load()
    val jedis = Jedis(config.database.host, config.database.port)
    jedis.select(3)
    val env = jsonMapper.readValue<DEnvironment>(file)

    jedis.persist("env:${env.code}", mapOf(
            "type" to env.type,
            "description" to env.name,
            "monitoring_url" to env.monitoringUrl + "@" + env.code,
            "active" to env.active.toString()
    ))
    var counter = 1.0
    env.instances.groupBy { it.svc }.forEach { (svc1, instances) ->
        val svc = if (env.name == "tradex-uatb" || env.name == "tradex-prod-est") svc1 + "est" else svc1
        val extraScore = if (env.name == "tradex-uatb" || env.name == "tradex-prod-est") 50 else 0
        val type = instances[0].type
        val mode = if (type == "redis" || env.code == "UAT") "LEADERSHIP" to "2" else "ONE_ONLY" to "1"
        jedis.persist("service:${env.code}:$svc", mapOf(
                "type" to type,
                "mode" to mode.first,
                "active_count" to mode.second,
                "component" to instances[0].project
        ))
        jedis.zadd("services:${env.code}", extraScore + counter++, svc)

        instances.map { instance ->
            jedis.persist("instance:${env.code}:$svc:${instance.id}", mapOf(
                    "host" to instance.host,
                    "folder" to instance.folder
            ))
        }
    }
    println("Migrated ${env.code}")
}

private fun reloadComponents(file: File) {
    val config = ConfigLoader(File("config/config.yaml")).load()
    val jedis = Jedis(config.database.host, config.database.port)
    jedis.select(1)
    jsonMapper.readValue<List<DComponent>>(file).forEach {
        //println(it)
        val systems = when {
            it.id.startsWith("tradex-") -> listOf("tradex")
            it.id.startsWith("postx-") -> listOf("postx")
            it.id.startsWith("lib-") -> listOf("libraries")
            else -> listOf("misc")
        }
        if (jedis.exists("component:${it.id}")) {
            println("${it.id} already exists")
        } else {
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
