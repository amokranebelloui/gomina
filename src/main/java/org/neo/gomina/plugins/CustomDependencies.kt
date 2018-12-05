package org.neo.gomina.plugins

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.neo.gomina.integration.nexus.NexusConnector

data class XRedisDep(var repository: String? = null, var topic: String? = null,
                     var type: String? = null, /*RW,RO*/var mode: String? = null, /*Mem*/
                     var redisName: String? = null, var responsible: Boolean? = null /*to register*/
)

data class XFidTxn(var transaction: String? = null, var service: String? = null, var fields: List<String>? = null)

data class XFidQuery(var query: String? = null, var service: String? = null, var patterns: List<String>? = null, var fields: List<String>? = null)

data class XFidPub(var queryField: String? = null, var service: String? = null, var patterns: List<String>? = null, var fields: List<String>? = null)

data class Api(var commands: Map<String, List<String>>? = null, var raised: Map<String, List<String>>? = null)

data class XDependencies (
        var fidessaTransactions: List<XFidTxn>? = null, var fidessaQueries: List<XFidQuery>? = null, var fidessaPublishers: List<XFidPub>? = null,
        var commands: Map<String, List<String>>? = null, var commandsInternal: Map<String, List<String>>? = null,
        var events: Map<String, List<String>>? = null,
        var redis: List<XRedisDep>? = null
)

data class XRawDeps(var component: String? = null,
                    var api: Api? = null,
                    var dependencies: XDependencies? = null,
                    var events: List<String>? = null
)

data class XComponentDeps(
        var component: String = "",
        var submittedCommands: MutableList<String> = mutableListOf(),
        var commandHandlers: MutableList<String> = mutableListOf(),
        var eventHandlers: MutableList<String> = mutableListOf(),
        var raisedEvents: MutableList<String> = mutableListOf(),
        var redis: MutableList<XRedisDep> = mutableListOf()
)

fun flat(rawDeps: XRawDeps): XComponentDeps {
    val componentDeps = XComponentDeps()
    componentDeps.component = rawDeps.component ?: ""

    rawDeps.api?.commands?.values?.forEach {
        commands -> componentDeps.commandHandlers.addAll(commands)
    }
    rawDeps.api?.raised?.values?.forEach {
        events -> componentDeps.raisedEvents.addAll(events)
    }
    rawDeps.dependencies?.commands?.values?.forEach {
        commands -> componentDeps.submittedCommands.addAll(commands)
    }
    rawDeps.dependencies?.commandsInternal?.values?.forEach {
        commands -> componentDeps.submittedCommands.addAll(commands)
    }
    rawDeps.dependencies?.events?.values?.forEach {
        events -> componentDeps.eventHandlers.addAll(events)
    }
    rawDeps.dependencies?.redis?.forEach {
        redis -> componentDeps.redis.add(redis)
    }
    return componentDeps
}

object XDepSource {

    val nexusAccess = NexusConnector("viw-facto-101", 8081, "releases", "snapshots")

    private val objectMapper = ObjectMapper()
    init {
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false)
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    }


    @Throws(Exception::class)
    @JvmOverloads operator fun get(group: String, artifact: String, version: String? = null): XRawDeps? {
        try {
            val result = objectMapper.readValue(nexusAccess.getContent(group, artifact, version), XRawDeps::class.java)
            result.component = artifact
            return result
        } catch (e: Exception) {
            println("Error $group $artifact $version")
            e.printStackTrace()
        }

        return null
    }
}
