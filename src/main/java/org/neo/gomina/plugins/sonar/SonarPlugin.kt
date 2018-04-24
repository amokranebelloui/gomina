package org.neo.gomina.plugins.sonar

import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.projects.ProjectDetail
import org.neo.gomina.core.projects.ProjectDetailRepository
import org.neo.gomina.core.projects.ProjectsExt
import org.neo.gomina.model.project.Projects
import org.neo.gomina.plugins.sonar.connectors.DummySonarConnector
import org.neo.gomina.plugins.sonar.connectors.HttpSonarConnector
import javax.inject.Inject

class SonarConnectors {
    @Inject private lateinit var sonarConfig: SonarConfig

    fun getConnector(server: String): SonarConnector? {
        return sonarConfig.serverMap[server] ?. let {
            when (it.mode) {
                "dummy" -> DummySonarConnector()
                "http" -> HttpSonarConnector(it.url)
                else -> null
            }
        }
    }
}

private fun ProjectDetail.apply(sonarIndicators: SonarIndicators?) {
    this.loc = sonarIndicators?.loc
    this.coverage = sonarIndicators?.coverage
}

class SonarPlugin : ProjectsExt {

    @Inject private lateinit var projects: Projects
    @Inject private lateinit var connectors: SonarConnectors

    @Inject lateinit var projectDetailRepository: ProjectDetailRepository

    override fun init() {
        logger.info("Initializing Sonar Data ...")
        reload()
        logger.info("Sonar Data initialized")
    }

    fun reload() {
        val map = projects.getProjects()
                .map { p -> p.sonarServer }
                .distinct()
                .mapNotNull { srv -> connectors.getConnector(srv)?.let { Pair(srv, it.getMetrics()) } }
                .toMap()

        for (project in projects.getProjects()) {
            val sonarIndicators = map[project.sonarServer]?.get(project.maven)

            val projectDetail = projectDetailRepository.getProject(project.id)
            if (projectDetail != null) {
                projectDetail.apply(sonarIndicators)
            }
        }
    }

    companion object {
        private val logger = LogManager.getLogger(SonarPlugin::class.java)
    }
}