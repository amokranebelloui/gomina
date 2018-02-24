package org.neo.gomina.model.inventory.file

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.inventory.Environment
import org.neo.gomina.model.inventory.InvInstance
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.inventory.Service
import java.io.File
import java.util.regex.Pattern


class FileInventory : Inventory {

    companion object {
        private val logger = LogManager.getLogger(FileInventory.javaClass)
    }

    private val yamlMapper = ObjectMapper(YAMLFactory())
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)

    private val jsonMapper = ObjectMapper(JsonFactory())
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)

    private val inventoryDir: File
    private val pattern: Pattern

    @Inject constructor(
            @Named("inventory.dir") inventoryDir: String,
            @Named("inventory.filter") inventoryFilter: String
    ) {
        this.inventoryDir = File(inventoryDir)
        this.pattern = Pattern.compile(inventoryFilter)
        logger.info("FileInventory dir:${this.inventoryDir} filter:${this.pattern}")
    }

    constructor(inventoryDir: String) : this (inventoryDir, "env\\.(.*?)\\.yaml")

    private fun getAllEnvironments(): Map<String, Environment> {
        val files = if (inventoryDir.isDirectory) inventoryDir.listFiles() else emptyArray()
        return files.filter { pattern.matcher(it.name).find() }
                .map { file -> readEnv(file) }
                .filterNotNull()
                .map { env -> env.id to env }
                .toMap()
    }

    fun readEnv(file: File): Environment? {
        try {
            return when (file.extension) {
                "yaml" -> yamlMapper.readValue(java.io.File("$inventoryDir/${file.name}"))
                "json" -> map(jsonMapper.readValue(java.io.File("$inventoryDir/${file.name}")))
                else -> null
            }
        }
        catch (e: Exception) {
            logger.error("Cannot read env $file", e)
            return null
        }
    }

    private fun map(old: DEnvironment): Environment {
        val services = old.instances
                .groupBy { i -> i.svc }
                .map { (svc, instances) -> Triple(svc, instances.first(), instances) }
                .map { (svc, i, instances) ->
                    Service(svc, i.type, i.project, instances.map { InvInstance(it.id, it.host, it.folder) })
                }

        return Environment(old.code, old.name, old.type, old.monitoringUrl, old.active, services)
    }

    override fun getEnvironments(): Collection<Environment> {
        return getAllEnvironments().values
    }

    override fun getEnvironment(env: String): Environment? {
        return getAllEnvironments()[env]
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
