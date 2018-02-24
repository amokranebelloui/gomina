package org.neo.gomina.model.inventory.file

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.inventory.Environment
import org.neo.gomina.model.inventory.Inventory
import java.io.File
import java.util.regex.Pattern


class FileInventory : Inventory {

    companion object {
        private val logger = LogManager.getLogger(FileInventory.javaClass)
    }

    private val mapper = ObjectMapper(YAMLFactory())
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

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
                .map { file -> mapper.readValue<Environment>(File("$inventoryDir/${file.name}")) }
                .map { env -> env.id to env }
                .toMap()
    }

    override fun getEnvironments(): Collection<Environment> {
        return getAllEnvironments().values
    }

    override fun getEnvironment(env: String): Environment? {
        return getAllEnvironments()[env]
    }

}