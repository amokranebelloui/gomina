package org.neo.gomina.integration.jenkins

import org.neo.gomina.integration.jenkins.jenkins.BuildStatus
import org.neo.gomina.model.component.Component
import org.neo.gomina.utils.Cache
import javax.inject.Inject

class JenkinsService {

    @Inject private lateinit var jenkinsConfig: JenkinsConfig
    @Inject private lateinit var jenkinsConnector: JenkinsConnector

    private val jenkinsCache = Cache<BuildStatus>("jenkins")

    fun getStatus(component: Component, fromCache: Boolean = false): BuildStatus? {
        val root = jenkinsConfig.serverMap[component.jenkinsServer]?.location
        val url = "$root${component.jenkinsJob}"
        // FIXME Return something when failing to retrieve status
        return jenkinsCache.get(url, fromCache) { jenkinsConnector.getStatus(url) }
    }

}