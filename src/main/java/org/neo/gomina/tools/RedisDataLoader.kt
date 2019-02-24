package org.neo.gomina.tools

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.neo.gomina.model.component.Component
import redis.clients.jedis.Jedis
import java.io.File

fun main(args: Array<String>) {
    val jsonMapper = ObjectMapper(JsonFactory())
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    val file = "components.json"
    val data = jsonMapper.readValue<List<Component>>(File("data/$file"))
    val jedis = Jedis("localhost", 6379)
    jedis.select(1)
    data.forEach {
        val mapOf: Map<String, String>? = listOfNotNull(
                it.label?.let { "label" to it },
                it.type?.let { "type" to it },
                "systems" to (it.systems.joinToString(", ")),
                "languages" to (it.languages.joinToString(", ")),
                "tags" to (it.tags.joinToString(", ")),
                "scm_type" to (it.scm?.type ?: ""),
                "scm_url" to (it.scm?.url ?: ""),
                "scm_path" to (it.scm?.path ?: ""),
                "scm_username" to (it.scm?.username ?: ""),
                "scm_password_alias" to (it.scm?.passwordAlias ?: ""),
                "maven" to (it.maven ?: ""),
                "sonarServer" to (it.sonarServer),
                "jenkinsServer" to (it.jenkinsServer),
                "jenkinsJob" to (it.jenkinsJob ?: "")
                //"disabled" to (it.jenkinsJob ?: "")
        )
        .toMap()
        jedis.hmset("component:${it.id}", mapOf)
    }
}