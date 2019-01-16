package org.neo.gomina.plugins

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.neo.gomina.model.inventory.Environment
import org.neo.gomina.model.inventory.Instance
import org.neo.gomina.model.inventory.Service
import java.io.File

fun main(args: Array<String>) {
    val jsonMapper = ObjectMapper(JsonFactory())
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
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
                Service(svc = svc, type = i.type, componentId = i.project, instances = instances.map { Instance(it.id, it.host, it.folder) })
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
