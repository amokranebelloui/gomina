package org.neo.gomina.integration.jenkins

import org.apache.commons.codec.net.URLCodec
import org.neo.gomina.integration.jenkins.jenkins.BuildStatus
import org.neo.gomina.model.component.Component
import org.neo.gomina.utils.Cache
import javax.inject.Inject

class JenkinsService {

    @Inject private lateinit var jenkinsConfig: JenkinsConfig
    @Inject private lateinit var jenkinsConnector: JenkinsConnector

    private val jenkinsCache = Cache<BuildStatus>("jenkins")

    fun getStatus(component: Component, fromCache: Boolean = false): BuildStatus? {
        return if (component.jenkinsJob?.isNotBlank() == true) {
            val root = jenkinsConfig.serverMap[component.jenkinsServer]?.location
            val url = "$root${URLCodec().encode(component.jenkinsJob).replace("+", "%20")}"
            // FIXME Return something when failing to retrieve status
            return jenkinsCache.get(url, fromCache) { jenkinsConnector.getStatus(url) }
        }
        else null
    }

}