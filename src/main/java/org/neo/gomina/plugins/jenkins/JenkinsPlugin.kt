package org.neo.gomina.plugins.jenkins

import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.projects.ProjectDetail
import org.neo.gomina.core.projects.ProjectDetailRepository
import org.neo.gomina.integration.jenkins.JenkinsConnector
import org.neo.gomina.integration.jenkins.jenkins.BuildStatus
import org.neo.gomina.model.project.Projects
import org.neo.gomina.plugins.Plugin
import org.neo.gomina.utils.Cache
import java.util.*
import javax.inject.Inject


data class JenkinsServer(val id: String = "", val location: String = "")

data class JenkinsConfig(val servers: List<JenkinsServer> = ArrayList()) {
    val serverMap = servers.associateBy { it.id }
}

private fun ProjectDetail.apply(url:String, status:BuildStatus?) {
    this.jenkinsUrl = url
    this.buildNumber = status?.id
    this.buildStatus = if (status?.building == true) "BUILDING" else status?.result
    this.buildTimestamp = status?.timestamp
}

class JenkinsPlugin : Plugin {

    @Inject private lateinit var projects: Projects
    @Inject private lateinit var jenkinsConfig: JenkinsConfig
    @Inject private lateinit var jenkinsConnector: JenkinsConnector

    private val jenkinsCache = Cache<BuildStatus>("jenkins")

    @Inject lateinit var projectDetailRepository: ProjectDetailRepository

    override fun init() {
        logger.info("Initializing Jenkins data ...")

        projects.getProjects()
                .mapNotNull { projectDetailRepository.getProject(it.id) ?. let { detail -> Pair(it, detail) } }
                .forEach { (project, detail) ->
                    val root = jenkinsConfig.serverMap[project.jenkinsServer]?.location
                    val url = "$root${project.jenkinsJob}"
                    val status = jenkinsCache.getOrLoad(url) { jenkinsConnector.getStatus(url) }
                    detail.apply(url, status)
                }
        logger.info("Jenkins data initialized")
    }

    fun reload(projectId:String) {
        projects.getProject(projectId)?.let { project ->
            projectDetailRepository.getProject(projectId)?.let { detail ->
                val root = jenkinsConfig.serverMap[project.jenkinsServer]?.location
                val url = "$root${project.jenkinsJob}"
                val status = jenkinsConnector.getStatus(url)
                detail.apply(url, status)
                status?.let { jenkinsCache.cache(url, status) }
            }
        }

    }

    companion object {
        private val logger = LogManager.getLogger(JenkinsPlugin::class.java)
    }
}