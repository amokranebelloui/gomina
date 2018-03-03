package org.neo.gomina.plugins.sonar

import org.neo.gomina.model.project.Projects
import org.neo.gomina.core.projects.ProjectDetail
import org.neo.gomina.core.projects.ProjectSet
import org.neo.gomina.core.projects.ProjectsExt
import javax.inject.Inject

class SonarPlugin : ProjectsExt {

    @Inject private lateinit var projects: Projects
    @Inject private lateinit var sonarConnector: SonarConnector

    override fun onGetProjects(projectSet: ProjectSet) {
        val sonarIndicatorsMap = sonarConnector.getMetrics()

        for (project in projects.getProjects()) {
            val sonarIndicators = sonarIndicatorsMap[project.maven]

            val projectDetail = projectSet.get(project.id)
            if (projectDetail != null) {
                projectDetail.apply(sonarIndicators)
            }
        }
    }

    override fun onGetProject(projectId: String, projectDetail: ProjectDetail) {
        projects.getProject(projectId)
                ?.let { sonarConnector.getMetrics()[it.maven] }
                ?.let { projectDetail.apply(it) }
    }

    private fun ProjectDetail.apply(sonarIndicators: SonarIndicators?) {
        this.loc = sonarIndicators?.loc
        this.coverage = sonarIndicators?.coverage
    }

}