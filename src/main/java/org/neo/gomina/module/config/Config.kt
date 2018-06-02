package org.neo.gomina.module.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

data class InventoryConfig(var projectsFile: String = "",
                           var hostsFile: String = "",
                           var inventoryDir: String = "",
                           var inventoryFilter: String = "")
data class MonitoringConfig(var timeout: Int = 5)
data class EventsConfig(var host: String = "localhost", var port: Int = 9200)

data class Config (

    var name: String? = null,
    var passwordsFile: String? = null,

    var inventory: InventoryConfig = InventoryConfig(),
    var monitoring: MonitoringConfig = MonitoringConfig(),
    var events: EventsConfig = EventsConfig()
)

class ConfigLoader {

    private val mapper = ObjectMapper(YAMLFactory())
            .registerModule(KotlinModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun load(): Config {
        val configFile = File("config/config.yaml")
        return mapper.readValue(configFile)
    }
}

fun main(args: Array<String>) {
    println(ConfigLoader().load())
}