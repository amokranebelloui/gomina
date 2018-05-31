package org.neo.gomina.plugins.jenkins

import org.apache.logging.log4j.LogManager
import org.neo.gomina.api.projects.ProjectDetail
import org.neo.gomina.integration.jenkins.JenkinsConfig
import org.neo.gomina.integration.jenkins.JenkinsConnector
import org.neo.gomina.integration.jenkins.jenkins.BuildStatus
import org.neo.gomina.model.project.Project
import org.neo.gomina.model.project.Projects
import org.neo.gomina.utils.Cache
import javax.inject.Inject

private fun ProjectDetail.apply(url:String, status:BuildStatus?) {
    this.jenkinsUrl = url
    this.buildNumber = status?.id
    this.buildStatus = if (status?.building == true) "BUILDING" else status?.result
    this.buildTimestamp = status?.timestamp
}

class JenkinsPlugin {

    @Inject private lateinit var projects: Projects
    @Inject private lateinit var jenkinsConfig: JenkinsConfig
    @Inject private lateinit var jenkinsConnector: JenkinsConnector

    private val jenkinsCache = Cache<BuildStatus>("jenkins")

    fun enrich(project: Project, detail: ProjectDetail) {
        val root = jenkinsConfig.serverMap[project.jenkinsServer]?.location
        val url = "$root${project.jenkinsJob}"
        val status = jenkinsCache.getOrLoad(url) { jenkinsConnector.getStatus(url) }
        detail.apply(url, status)
    }

    fun reload(projectId:String) {
        logger.info("Reload Jenkins data for $projectId ...")
        projects.getProject(projectId)?.let { project ->
            val root = jenkinsConfig.serverMap[project.jenkinsServer]?.location
            val url = "$root${project.jenkinsJob}"
            val status = jenkinsConnector.getStatus(url)
            status?.let { jenkinsCache.cache(url, status) }
        }
    }

    companion object {
        private val logger = LogManager.getLogger(JenkinsPlugin::class.java)
    }
}