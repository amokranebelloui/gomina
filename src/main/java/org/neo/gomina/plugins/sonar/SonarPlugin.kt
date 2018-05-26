package org.neo.gomina.plugins.sonar

import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.projects.ProjectDetail
import org.neo.gomina.core.projects.ProjectDetailRepository
import org.neo.gomina.integration.sonar.SonarConnectors
import org.neo.gomina.integration.sonar.SonarIndicators
import org.neo.gomina.model.project.Projects
import org.neo.gomina.plugins.Plugin
import javax.inject.Inject

private fun ProjectDetail.apply(sonarIndicators: SonarIndicators?) {
    this.loc = sonarIndicators?.loc
    this.coverage = sonarIndicators?.coverage
}

class SonarPlugin : Plugin {

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
            projectDetail?.apply(sonarIndicators)
        }
    }

    companion object {
        private val logger = LogManager.getLogger(SonarPlugin::class.java)
    }
}