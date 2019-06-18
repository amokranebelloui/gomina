package org.neo.gomina.integration.sonar

import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.component.Component
import org.neo.gomina.model.component.ComponentRepo
import javax.inject.Inject

class SonarService {
    companion object {
        private val logger = LogManager.getLogger(SonarService.javaClass)
    }

    @Inject private lateinit var connectors: SonarConnectors
    @Inject private lateinit var sonarConfig: SonarConfig
    @Inject private lateinit var componentRepo: ComponentRepo

    fun servers() = sonarConfig.servers.map { it.id }

    fun url(component: Component) = sonarConfig.serverMap[component.sonarServer]?.url
            ?.let { "$it/dashboard/index/${component.artifactId}" }

    fun reload(component: Component) {
        val metrics = connectors.getConnector(component.sonarServer)?.getMetrics(component.artifactId)
        componentRepo.getAll().forEach { component ->
            try {
                component.artifactId
                        ?.let { metrics?.get(it) }
                        ?.let { metrics -> componentRepo.updateCodeMetrics(component.id, metrics.loc, metrics.coverage) }
            }
            catch (e: Exception) {
                logger.error("", e)
            }
        }
    }

}