package org.neo.gomina.persistence.model

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.inventory.*
import java.io.File
import java.util.regex.Pattern

class InventoryFile : Inventory, AbstractFileRepo {

    companion object {
        private val logger = LogManager.getLogger(InventoryFile.javaClass)
    }

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
        return try {
            when (file.extension) {
                "yaml" -> yamlMapper.readValue(java.io.File("$inventoryDir/${file.name}"))
                "json" -> jsonMapper.readValue(java.io.File("$inventoryDir/${file.name}"))
                else -> null
            }
        }
        catch (e: Exception) {
            logger.error("Cannot read env $file", e)
            null
        }
    }

    override fun getEnvironments(): Collection<Environment> = getAllEnvironments().values
    override fun getEnvironment(env: String): Environment? = getAllEnvironments()[env]

}
