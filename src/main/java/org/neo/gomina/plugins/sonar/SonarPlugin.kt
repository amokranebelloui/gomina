package org.neo.gomina.plugins.sonar

import org.apache.logging.log4j.LogManager
import org.neo.gomina.api.projects.ProjectDetail
import org.neo.gomina.integration.sonar.SonarConfig
import org.neo.gomina.integration.sonar.SonarConnectors
import org.neo.gomina.integration.sonar.SonarIndicators
import org.neo.gomina.model.project.Project
import org.neo.gomina.model.project.Projects
import org.neo.gomina.utils.Cache
import javax.inject.Inject

private fun ProjectDetail.apply(project: Project, serverUrl: String?, sonarIndicators: SonarIndicators?) {
    val url = serverUrl ?: ""
    this.sonarUrl = "$url/dashboard/index/${project.maven}"
    this.loc = sonarIndicators?.loc
    this.coverage = sonarIndicators?.coverage
}

class SonarPlugin {

    @Inject private lateinit var projects: Projects
    @Inject private lateinit var connectors: SonarConnectors
    @Inject private lateinit var sonarConfig: SonarConfig

    private val sonarCache = Cache<Map<String, SonarIndicators>>("sonar")

    fun enrich(project: Project, detail: ProjectDetail) {
        val metrics = sonarCache.getOrLoad(project.sonarServer) {
            connectors.getConnector(project.sonarServer)?.getMetrics()
        }
        val serverUrl = sonarConfig.serverMap[project.sonarServer]?.url
        metrics ?. let { detail.apply(project, serverUrl, metrics[project.maven]) }
    }

    fun reload() {
        logger.info("Reloading Sonar data ...")
        projects.getProjects()
                .map { it.sonarServer }
                .distinct()
                .forEach { svc ->
                    val metrics = connectors.getConnector(svc)?.getMetrics()
                    metrics?. let { metrics -> sonarCache.cache(svc, metrics) }
                }
    }

    companion object {
        private val logger = LogManager.getLogger(SonarPlugin::class.java)
    }
}