package org.neo.gomina.integration.jenkins

import org.neo.gomina.integration.jenkins.jenkins.BuildStatus
import org.neo.gomina.model.project.Project
import org.neo.gomina.utils.Cache
import javax.inject.Inject

class JenkinsService {

    @Inject private lateinit var jenkinsConfig: JenkinsConfig
    @Inject private lateinit var jenkinsConnector: JenkinsConnector

    private val jenkinsCache = Cache<BuildStatus>("jenkins")

    fun getStatus(project: Project, fromCache: Boolean = false): BuildStatus? {
        val root = jenkinsConfig.serverMap[project.jenkinsServer]?.location
        val url = "$root${project.jenkinsJob}"
        // FIXME Return something when failing to retrieve status
        return jenkinsCache.get(url, fromCache) { jenkinsConnector.getStatus(url) }
    }

}