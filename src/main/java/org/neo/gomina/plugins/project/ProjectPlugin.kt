package org.neo.gomina.plugins.project

import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.projects.ProjectDetail
import org.neo.gomina.model.project.Project
import org.neo.gomina.model.project.Projects
import org.neo.gomina.plugins.Plugin
import javax.inject.Inject

fun ProjectDetail.apply(project: Project) {
    this.label = project.label ?: project.id
    this.type = project.type
    this.tags = project.tags
    this.scmRepo = project.svnRepo
    this.scmLocation = project.svnUrl
    this.mvn = project.maven
    this.jenkinsServer = project.jenkinsServer
    this.jenkinsJob = project.jenkinsJob
}

class ProjectPlugin : Plugin {

    @Inject private lateinit var projects: Projects

    companion object {
        private val logger = LogManager.getLogger(ProjectPlugin::class.java)
    }
}