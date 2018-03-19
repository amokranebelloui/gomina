package org.neo.gomina.plugins.jenkins

import org.neo.gomina.core.projects.ProjectDetail
import org.neo.gomina.core.projects.ProjectSet
import org.neo.gomina.core.projects.ProjectsExt
import org.neo.gomina.model.project.Projects
import java.util.*
import javax.inject.Inject


data class JenkinsServer(val id: String = "", val location: String = "")

data class JenkinsConfig(val servers: List<JenkinsServer> = ArrayList()) {
    val serverMap = servers.associateBy { it.id }
}

class JenkinsPlugin : ProjectsExt {

    @Inject private lateinit var projects: Projects
    @Inject private lateinit var jenkinsConfig: JenkinsConfig

    override fun onGetProjects(projectSet: ProjectSet) {
        for (project in projects.getProjects()) {
            val projectDetail = projectSet.get(project.id)
            if (projectDetail != null) {
                val root = jenkinsConfig.serverMap[project.jenkinsServer]?.location
                projectDetail.jenkinsUrl = "$root${project.jenkinsJob}"
            }
        }
    }

    override fun onGetProject(projectId: String, projectDetail: ProjectDetail) {
        projects.getProject(projectId) ?. let {
            val root = jenkinsConfig.serverMap[it.jenkinsServer]?.location
            projectDetail.jenkinsUrl = "$root${it.jenkinsJob}"
        }
    }

}