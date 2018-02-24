package org.neo.gomina.model.inventory.file

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.inventory.Environment
import org.neo.gomina.model.inventory.Inventory
import java.io.File
import java.util.*
import java.util.regex.Pattern

class FileInventory : Inventory {

    companion object {
        private val logger = LogManager.getLogger(FileInventory.javaClass)
    }

    private val mapper = ObjectMapper(YAMLFactory())
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private val pattern = Pattern.compile("env\\.(.*?)\\.yaml")

    private fun getFileCodes(): List<String> {
        val envs = ArrayList<String>()
        val data = File("data")
        if (data.isDirectory) {
            for (file in data.listFiles()!!) {
                val matcher = pattern.matcher(file.name)
                if (matcher.find()) {
                    envs.add(matcher.group(1))
                }
            }
        }
        return envs
    }


    private fun getAllEnvironments(): Map<String, Environment> {
        val environments = HashMap<String, Environment>()
        for (fileCode in getFileCodes()) {
            val environment = mapper.readValue<Environment>(File("data/env.$fileCode.yaml"))
            environments.put(environment.id, environment)
        }
        return environments
    }

    override fun getEnvironments(): Collection<Environment> {
        return getAllEnvironments().values
    }

    override fun getEnvironment(env: String): Environment? {
        return getAllEnvironments()[env]
    }

}