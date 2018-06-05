package org.neo.gomina.integration.sonar

import org.neo.gomina.model.project.Project
import org.neo.gomina.utils.Cache
import javax.inject.Inject

class SonarService {

    @Inject private lateinit var connectors: SonarConnectors
    @Inject private lateinit var sonarConfig: SonarConfig

    private val sonarCache = Cache<Map<String, SonarIndicators>>("sonar")

    fun getSonar(project: Project, fromCache: Boolean): SonarIndicators? {
        val metrics = sonarCache.get(project.sonarServer, fromCache) {
            connectors.getConnector(project.sonarServer)?.getMetrics()
        }

        return metrics?.get(project.maven)?.apply {
            val serverUrl = sonarConfig.serverMap[project.sonarServer]?.url
            val baseUrl = serverUrl ?: ""
            this.sonarUrl = "$baseUrl/dashboard/index/${project.maven}"
        }
    }

    fun reload(sonarServer: String) {
        val metrics = connectors.getConnector(sonarServer)?.getMetrics()
        metrics?.let { metrics -> sonarCache.cache(sonarServer, metrics) }
    }

}