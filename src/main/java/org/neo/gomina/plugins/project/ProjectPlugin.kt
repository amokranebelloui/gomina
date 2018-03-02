package org.neo.gomina.plugins.project

import org.neo.gomina.model.project.Project
import org.neo.gomina.model.project.Projects
import org.neo.gomina.model.projects.ProjectDetail
import org.neo.gomina.model.projects.ProjectSet
import org.neo.gomina.model.projects.ProjectsExt
import javax.inject.Inject

class ProjectPlugin : ProjectsExt {

    @Inject private lateinit var projects: Projects

    override fun onGetProjects(projectSet: ProjectSet) {
        for (project in projects.getProjects()) {
            val projectDetail = projectSet.ensure(project.id)
            //projectDetail.id = project.id
            projectDetail.apply(project)
        }
    }

    override fun onGetProject(projectId: String, projectDetail: ProjectDetail) {
        projects.getProject(projectId)?.let {
            projectDetail.apply(it)
        }
    }

    private fun ProjectDetail.apply(project: Project) {
        this.label = project.label ?: project.id
        this.type = project.type
        this.repo = project.svnRepo
        this.svn = project.svnUrl
        this.mvn = project.maven
        this.jenkins = project.jenkinsJob
    }

}