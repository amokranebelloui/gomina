package org.neo.gomina.plugins.project

import org.neo.gomina.core.projects.ProjectDetail
import org.neo.gomina.model.project.Project

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
