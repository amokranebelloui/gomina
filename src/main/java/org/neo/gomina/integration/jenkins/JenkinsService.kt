package org.neo.gomina.integration.jenkins

import org.apache.commons.codec.net.URLCodec
import org.neo.gomina.model.component.Component
import org.neo.gomina.model.component.ComponentRepo
import javax.inject.Inject

class JenkinsService {

    @Inject private lateinit var jenkinsConfig: JenkinsConfig
    @Inject private lateinit var jenkinsConnector: JenkinsConnector
    @Inject private lateinit var componentRepo: ComponentRepo

    fun servers() = jenkinsConfig.servers.map { it.id }

    fun url(component: Component): String? {
        return if (component.jenkinsJob?.isNotBlank() == true) {
            val root = jenkinsConfig.serverMap[component.jenkinsServer]?.location
            return "$root${URLCodec().encode(component.jenkinsJob).replace("+", "%20")}"
        }
        else null
    }

    fun reload(component: Component) {
        url(component)?.let { url ->
            // FIXME Return something when failing to retrieve status
            jenkinsConnector.getStatus(url)?.let {
                componentRepo.updateBuildStatus(component.id, it.id, it.result, it.building, it.timestamp)
            }
        }
    }

}