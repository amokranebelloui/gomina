package org.neo.gomina.plugins.sonar

import org.neo.gomina.model.project.Projects
import org.neo.gomina.core.projects.ProjectDetail
import org.neo.gomina.core.projects.ProjectSet
import org.neo.gomina.core.projects.ProjectsExt
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

class SonarPlugin : ProjectsExt {

    @Inject private lateinit var projects: Projects
    @Inject private lateinit var connectors: SonarConnectors

    override fun onGetProjects(projectSet: ProjectSet) {

        val map = projects.getProjects()
                .map { p -> p.sonarServer }
                .distinct()
                .mapNotNull { srv -> connectors.getConnector(srv)?.let { Pair(srv, it.getMetrics()) } }
                .toMap()

        for (project in projects.getProjects()) {
            val sonarIndicators = map[project.sonarServer]?.get(project.maven)

            val projectDetail = projectSet.get(project.id)
            if (projectDetail != null) {
                projectDetail.apply(sonarIndicators)
            }
        }
    }

    override fun onGetProject(projectId: String, projectDetail: ProjectDetail) {
        projects.getProject(projectId)
                ?.let { p ->
                    connectors.getConnector(p.sonarServer) ?. let { it.getMetrics()[p.maven] }
                }
                ?.let { projectDetail.apply(it) }
    }

    private fun ProjectDetail.apply(sonarIndicators: SonarIndicators?) {
        this.loc = sonarIndicators?.loc
        this.coverage = sonarIndicators?.coverage
    }

}