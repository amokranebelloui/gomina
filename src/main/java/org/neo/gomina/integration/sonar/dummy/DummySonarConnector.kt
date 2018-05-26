package org.neo.gomina.integration.sonar.dummy

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.sonar.SonarConnector
import org.neo.gomina.integration.sonar.SonarIndicators
import java.io.File
import java.io.IOException
import java.util.*

class DummySonarConnector : SonarConnector {

    private val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

    val file = File("datadummy/projects.sonar.yaml")

    override fun getMetrics(): Map<String, SonarIndicators> {
        logger.info("Get metrics for dummy")
        return getMetrics(null)
    }

    override fun getMetrics(resource: String?): Map<String, SonarIndicators> {
        val indicators = HashMap<String, SonarIndicators>()
        try {
            //val value: TypeReference<List<SonarIndicators>> = object : TypeReference<List<SonarIndicators>>() {}
            for (project in mapper.readValue<List<SonarIndicators>>(file)) {
                indicators.put(project.code, project)
            }
        }
        catch (e: IOException) {
            logger.error("Error retrieving Sonar data", e)
        }

        return indicators
    }

    companion object {
        private val logger = LogManager.getLogger(DummySonarConnector::class.java)
    }
}