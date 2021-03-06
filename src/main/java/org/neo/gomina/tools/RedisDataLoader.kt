package org.neo.gomina.tools

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.neo.gomina.model.component.Component
import org.neo.gomina.model.dependency.Interactions
import org.neo.gomina.model.user.User
import redis.clients.jedis.Jedis
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

private val jsonMapper = ObjectMapper(JsonFactory())
        .registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
        .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)

private val host: String = "localhost"
private val port: Int = 7080
private val jedis = Jedis(host, port)

private val md = MessageDigest.getInstance("SHA-512")
private val defaultHash = md.digest("!!password12".toByteArray(StandardCharsets.UTF_8)).toString(StandardCharsets.UTF_8)

fun main(args: Array<String>) {
    //loadUsers()
    //defaultPasswordsIfEmpty()
    //jedis.select(0); jedis.hset("user:ab6743", "password_hash", defaultHash)
    //loadComponents()
    //loadInteractions()

    /*
    jedis.select(1)
    jedis.keys("component:*").forEach {
        val maven = jedis.hget(it, "maven")
        if (maven != null) jedis.hset(it, "artifact_id", maven)
    }
    */
}

private fun loadUsers() {
    load(jsonMapper.readValue<List<User>>(File("data/users.json")), 0, "user") {
        Data(
                it.id,
                listOfNotNull(
                        it.login?.let { "login" to it },
                        it.shortName?.let { "short_name" to it },
                        it.firstName?.let { "first_name" to it },
                        it.lastName?.let { "last_name" to it },
                        "accounts" to (it.accounts.joinToString(", "))
                        //"disabled" to (it.jenkinsJob ?: "")
                ).toMap()
        )
    }
}

private fun defaultPasswordsIfEmpty() {
    jedis.select(0)
    jedis.keys("user:*").forEach {
        jedis.hsetnx(it, "password_hash", defaultHash)
    }
}

private fun loadComponents() {
    load(jsonMapper.readValue<List<Component>>(File("data/components.json")), 1, "component") {
        Data(
                it.id,
                listOfNotNull(
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
                        "artifact_id" to (it.artifactId ?: ""),
                        "sonar_server" to (it.sonarServer),
                        "jenkins_server" to (it.jenkinsServer),
                        "jenkins_job" to (it.jenkinsJob ?: "")
                        //"disabled" to (it.jenkinsJob ?: "")
                ).toMap()
        )
    }
}

private fun loadInteractions() {
    jedis.select(2)
    val source = "manual"

    val interactions: List<Interactions> = jsonMapper.readValue(File("data/interactions.json"))

    interactions.forEach { service ->
        service.exposed.forEach {
            val key = "api:$source:${service.serviceId}:exposes:${it.name}"
            val map = mapOf(
                    "name" to it.name,
                    "type" to it.type
            )
            println("exposed. $key $map")
            jedis.hmset(key, map)
        }
        service.used.forEach {
            val key = "api:$source:${service.serviceId}:uses:${it.function.name}"
            val map = listOfNotNull(
                    "name" to it.function.name,
                    "type" to it.function.type,
                    it.usage?.usage?.let { "usage" to it.toString() }
            ).toMap()
            println("used. $key $map")
            jedis.hmset(key, map)
        }

    }
}

private data class Data(val id: String, val map: Map<String, String>)

private fun <T> load(items: List<T>, database: Int, prefix: String, f: (T) -> Data) {
    jedis.select(database)
    items.forEach { item: T ->
        val data = f(item)
        data.let { jedis.hmset("$prefix:${it.id}", it.map) }
    }
}
