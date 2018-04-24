package org.neo.gomina.plugins.jenkins

import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.projects.ProjectDetailRepository
import org.neo.gomina.model.project.Projects
import org.neo.gomina.plugins.Plugin
import java.util.*
import javax.inject.Inject


data class JenkinsServer(val id: String = "", val location: String = "")

data class JenkinsConfig(val servers: List<JenkinsServer> = ArrayList()) {
    val serverMap = servers.associateBy { it.id }
}

class JenkinsPlugin : Plugin {

    @Inject private lateinit var projects: Projects
    @Inject private lateinit var jenkinsConfig: JenkinsConfig

    @Inject lateinit var projectDetailRepository: ProjectDetailRepository

    override fun init() {
        logger.info("Initializing Jenkins data ...")
        for (project in projects.getProjects()) {
            val projectDetail = projectDetailRepository.getProject(project.id)
            if (projectDetail != null) {
                val root = jenkinsConfig.serverMap[project.jenkinsServer]?.location
                projectDetail.jenkinsUrl = "$root${project.jenkinsJob}"
            }
        }
        logger.info("Jenkins data initialized")
    }

    companion object {
        private val logger = LogManager.getLogger(JenkinsPlugin::class.java)
    }
}