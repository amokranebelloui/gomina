package org.neo.gomina.integration.sonar

import org.neo.gomina.model.component.Component
import org.neo.gomina.utils.Cache
import javax.inject.Inject

class SonarService {

    @Inject private lateinit var connectors: SonarConnectors
    @Inject private lateinit var sonarConfig: SonarConfig

    private val sonarCache = Cache<Map<String, SonarIndicators>>("sonar")

    fun getSonar(component: Component, fromCache: Boolean): SonarIndicators? {
        val metrics = sonarCache.get(component.sonarServer, fromCache) {
            connectors.getConnector(component.sonarServer)?.getMetrics()
        }

        return metrics?.get(component.maven)?.apply {
            val serverUrl = sonarConfig.serverMap[component.sonarServer]?.url
            val baseUrl = serverUrl ?: ""
            this.sonarUrl = "$baseUrl/dashboard/index/${component.maven}"
        }
    }

    fun reload(sonarServer: String) {
        val metrics = connectors.getConnector(sonarServer)?.getMetrics()
        metrics?.let { metrics -> sonarCache.cache(sonarServer, metrics) }
    }

}