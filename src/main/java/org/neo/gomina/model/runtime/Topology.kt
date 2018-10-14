package org.neo.gomina.model.runtime

import org.neo.gomina.model.host.HostRepo
import org.neo.gomina.model.host.InstanceSshDetails
import org.neo.gomina.model.inventory.Environment
import org.neo.gomina.model.inventory.Instance
import org.neo.gomina.model.inventory.Service
import org.neo.gomina.model.monitoring.Monitoring
import org.neo.gomina.model.monitoring.RuntimeInfo
import org.neo.gomina.model.project.Project
import org.neo.gomina.model.project.Projects
import org.neo.gomina.model.scm.ScmDetails
import org.neo.gomina.model.scm.ScmRepos
import javax.inject.Inject

data class ExtInstance(
        val id: String,
        val project: Project?,
        val scmDetail: ScmDetails?,
        val service: Service,
        val instance: Instance?,
        val sshDetails: InstanceSshDetails?,
        val indicators: RuntimeInfo?
) {
    val expected get() = instance != null
    val notExpected get() = instance == null
}

class Topology {

    @Inject private lateinit var projects: Projects
    @Inject lateinit private var monitoring: Monitoring

    @Inject lateinit private var scmRepo: ScmRepos
    @Inject lateinit private var hostRepo: HostRepo

    fun buildExtInstances(env: Environment): List<ExtInstance> {
        val services = env ?. services ?. associateBy { it.svc }
        val inventory = env.services
                .flatMap { svc -> svc.instances.map { instance -> svc to instance } }
                .associateBy { (_, instance) -> instance.id }
        val monitoring = monitoring.instancesFor(env.id)
                .associateBy { it.instanceId }


        return merge(inventory, monitoring)
                .map { (id, instance, indicators) ->
                    val svc = instance?.first?.svc ?: indicators?.service ?: "x"
                    val service = services[svc] ?: Service(svc = svc, type = indicators?.type)
                    val project = service.project?.let { projects.getProject(it) }
                    val sshDetails = instance?.second?.let { hostRepo.getDetails(it) }
                    val scmDetail = project?.scm?.let { scmRepo.getScmDetails(it) }

                    ExtInstance(id, project, scmDetail, service, instance?.second, sshDetails, indicators)
                }
    }

}

// FIXME Detect Multiple running instances with same name

fun <T, R> merge(map1:Map<String, T>, map2:Map<String, R>): Collection<Triple<String, T?, R?>> {
    val result = mutableMapOf<String, Triple<String, T?, R?>>()
    map1.forEach { (id, t) -> result.put(id, Triple(id, t, map2[id])) }
    map2.forEach { (id, r) -> if (!map1.contains(id)) result.put(id, Triple(id, null, r)) }
    return result.values
}

fun main(args: Array<String>) {
    val m1 = mapOf("1" to 1, "2" to 2)
    val m2 = mapOf("2" to 20.2, "3" to 30.3)
    println(merge(m1, m2))
}


