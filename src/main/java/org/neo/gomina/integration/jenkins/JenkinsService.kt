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
        return url(component.jenkinsServer, component.jenkinsJob)
    }

    fun url(server: String, job: String?): String? {
        return if (job?.isNotBlank() == true) {
            val root = jenkinsConfig.serverMap[server]?.location
            return "$root${URLCodec().encode(job).replace("+", "%20")}"
        } else null
    }

    fun reload(component: Component) {
        url(component)?.let { url ->
            // FIXME Return something when failing to retrieve status
            jenkinsConnector.getStatus(url)?.let {
                componentRepo.updateBuildStatus(component.id, it.id, it.result, it.building, it.timestamp)
            }
        }
        component.branches.forEach { b ->
            url(b.buildServer, b.buildJob)?.let { url ->
                jenkinsConnector.getStatus(url)?.let {
                    componentRepo.updateBranchBuildStatus(component.id, b.name, it.id, it.result, it.building, it.timestamp)
                }
            }
        }
    }

}