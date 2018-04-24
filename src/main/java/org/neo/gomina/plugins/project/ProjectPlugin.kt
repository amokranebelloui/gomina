package org.neo.gomina.plugins.project

import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.projects.ProjectDetail
import org.neo.gomina.core.projects.ProjectDetailRepository
import org.neo.gomina.core.projects.ProjectsExt
import org.neo.gomina.model.project.Project
import org.neo.gomina.model.project.Projects
import javax.inject.Inject

private fun ProjectDetail.apply(project: Project) {
    this.label = project.label ?: project.id
    this.type = project.type
    this.scmRepo = project.svnRepo
    this.scmLocation = project.svnUrl
    this.mvn = project.maven
    this.jenkinsServer = project.jenkinsServer
    this.jenkinsJob = project.jenkinsJob
}

class ProjectPlugin : ProjectsExt {

    @Inject private lateinit var projects: Projects

    @Inject lateinit var projectDetailRepository: ProjectDetailRepository

    override fun init() {
        logger.info("Initializing projects ...")
        for (project in projects.getProjects()) {
            val projectDetail = ProjectDetail(project.id)
            projectDetail.apply(project)
            projectDetailRepository.addProject(projectDetail);
        }
        logger.info("Projects initialized")
    }

    companion object {
        private val logger = LogManager.getLogger(ProjectPlugin::class.java)
    }
}