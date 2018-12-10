package org.neo.gomina.module.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.elasticsearch.ElasticEventsProviderConfig
import org.neo.gomina.integration.jenkins.JenkinsConfig
import org.neo.gomina.integration.monitoring.MonitoringEventsProviderConfig
import org.neo.gomina.integration.sonar.SonarConfig
import java.io.File

data class InventoryConfig(var componentsFile: String = "",
                           var interactionsFile: String = "",
                           var workFile: String = "",
                           var hostsFile: String = "",
                           var inventoryDir: String = "",
                           var inventoryFilter: String = "")
data class MonitoringConfig(var timeout: Int = 5)
data class EventsConfig(
        var elasticSearch: List<ElasticEventsProviderConfig> = emptyList(),
        var internalMonitoring: List<MonitoringEventsProviderConfig> = emptyList()
) {
    fun all() = internalMonitoring + elasticSearch
}

data class Config (

        var name: String? = null,
        var passwordsFile: String? = null,
        val usersFile: String?,
        var domains: List<String> = emptyList(),
        val jiraUrl: String = "",
        var inventory: InventoryConfig = InventoryConfig(),
        var monitoring: MonitoringConfig = MonitoringConfig(),
        var events: EventsConfig = EventsConfig(),
        var jenkins: JenkinsConfig = JenkinsConfig(),
        var sonar: SonarConfig = SonarConfig()
)

class ConfigLoader {

    private val mapper = ObjectMapper(YAMLFactory())
            .registerModule(KotlinModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun load(): Config {
        val configFile = System.getProperty("gomina.config.file")
        val file = File(if (configFile.isNotBlank()) configFile else "config/config.yaml")
        logger.info("Loading configuration from $file")
        return mapper.readValue(file)
    }

    companion object {
        private val logger = LogManager.getLogger(ConfigLoader.javaClass)
    }

}

fun main(args: Array<String>) {
    println(ConfigLoader().load())
}