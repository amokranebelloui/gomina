package org.neo.gomina.model.runtime

import org.neo.gomina.model.component.Component
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.inventory.Environment
import org.neo.gomina.model.inventory.Instance
import org.neo.gomina.model.inventory.Service
import org.neo.gomina.model.monitoring.Monitoring
import org.neo.gomina.model.monitoring.RuntimeInfo
import org.neo.gomina.model.version.Version
import javax.inject.Inject

private data class EnvInstance(val envId: String, val service: Service, val instance: Instance)
private data class EnvService(val envId: String, val service: Service)
private data class EnvIndicators(val envId: String, val indicators: RuntimeInfo)

data class ExtInstance(
        val id: Pair<String, String>,
        val component: Component?,
        val service: Service,
        val instance: Instance?,
        val indicators: RuntimeInfo?
) {
    val completeId get() = id.first + "-" + id.second
    val envId get() = id.first
    val instanceId get() = id.second
    val expected get() = instance != null
    val notExpected get() = instance == null
    val deployedVersion get() = instance?.deployedVersion
    val runningVersion get() = indicators?.version
    val versions get() = listOfNotNull(deployedVersion, runningVersion)
    fun matchesVersion(version: Version): Boolean {
        return deployedVersion?.let { it.version == version.version } ?: false ||
                runningVersion?.let { it.version == version.version } ?: false
    }
}

class Topology {

    @Inject private lateinit var monitoring: Monitoring

    fun buildExtInstances(env: Environment, components: List<Component>): List<ExtInstance> {
        val services = env ?. services ?. associateBy { it.svc }
        val inventory = env.services
                .flatMap { svc -> svc.instances.map { instance -> svc to instance } }
                .associateBy { (_, instance) -> env.id to instance.id }
        val monitoring = monitoring.instancesFor(env.id)
                .associateBy { env.id to it.instanceId }

        val componentMap = components.associateBy { it.id }

        // FIXME associate real time and monitoring by monitoring url@<env> and no more env.id
        return merge(inventory, monitoring)
                .map { (id, instance, indicators) ->
                    val svc = instance?.first?.svc ?: indicators?.service ?: "x"
                    val service = services[svc] ?: Service(svc = svc, type = indicators?.type, undefined = true)
                    val component = service.componentId?.let { componentMap[it] }

                    ExtInstance(id, component, service, instance?.second, indicators)
                }
    }

    fun buildExtInstances(component: Component, environments: Collection<Environment>): List<ExtInstance> {
        val services = environments
                .flatMap { env -> env.services.map { env to it } }
                .map { (env, service) -> EnvService(env.id, service) }.associateBy { it.envId to it.service.svc }

        val inventory = environments
                .flatMap { env -> env.services.map { env to it } }
                .flatMap { (env, svc) -> svc.instances.map { instance -> EnvInstance(env.id, svc, instance) } }
                .associateBy { envInstance -> envInstance.envId to envInstance.instance.id }

        val runtime = environments
                .flatMap { env -> monitoring.instancesFor(env.id).map { env.id to it } }
                .map { (envId, mon) -> EnvIndicators(envId, mon) }
                .associateBy { envIndicators -> envIndicators.envId to envIndicators.indicators.instanceId }

        val merge = merge(inventory, runtime)

        val result = merge
                .mapNotNull { (id, eInstance, eRuntime) ->
                    val svc = Service.safe(eInstance?.service?.svc ?: eRuntime?.indicators?.service)
                    val envService = eInstance?.let { services[it.envId to it.service.svc] }
                            ?: eRuntime?.let { services[it.envId to it.indicators.service] }
                            ?: EnvService(id.first, Service(svc = svc, type = eRuntime?.indicators?.type, undefined = true))
                    if (envService.service.componentId == component.id) {
                        ExtInstance(id.first to id.second, component, envService.service, eInstance?.instance, eRuntime?.indicators)
                    } else null
                }
        return result
    }

}

// FIXME Detect Multiple running instances with same name

fun <ID, T, R> merge(map1:Map<ID, T>, map2:Map<ID, R>): Collection<Triple<ID, T?, R?>> {
    val result = mutableMapOf<ID, Triple<ID, T?, R?>>()
    map1.forEach { (id, t) -> result.put(id, Triple(id, t, map2[id])) }
    map2.forEach { (id, r) -> if (!map1.contains(id)) result.put(id, Triple(id, null, r)) }
    return result.values
}

fun main(args: Array<String>) {
    val m1 = mapOf("1" to 1, "2" to 2)
    val m2 = mapOf("2" to 20.2, "3" to 30.3)
    println(merge(m1, m2))
}


