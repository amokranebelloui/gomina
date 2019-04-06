package org.neo.gomina.model.runtime

import org.neo.gomina.model.component.Component
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.inventory.Environment
import org.neo.gomina.model.inventory.Instance
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.inventory.Service
import org.neo.gomina.model.monitoring.Monitoring
import org.neo.gomina.model.monitoring.RuntimeInfo
import javax.inject.Inject

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
}

class Topology {

    @Inject private lateinit var inventory: Inventory
    @Inject private lateinit var componentRepo: ComponentRepo
    @Inject lateinit private var monitoring: Monitoring

    fun buildExtInstances(env: Environment): List<ExtInstance> {
        val services = env ?. services ?. associateBy { it.svc }
        val inventory = env.services
                .flatMap { svc -> svc.instances.map { instance -> svc to instance } }
                .associateBy { (_, instance) -> env.id to instance.id }
        val monitoring = monitoring.instancesFor(env.id)
                .associateBy { env.id to it.instanceId }

        // FIXME associate real time and monitoring by monitoring url@<env> and no more env.id
        return merge(inventory, monitoring)
                .map { (id, instance, indicators) ->
                    val svc = instance?.first?.svc ?: indicators?.service ?: "x"
                    val service = services[svc] ?: Service(svc = svc, type = indicators?.type)
                    val component = service.componentId?.let { componentRepo.get(it) }

                    ExtInstance(id, component, service, instance?.second, indicators)
                }
    }

    fun buildExtInstances(componentId: String): List<ExtInstance> {
        val inv = inventory.getEnvironments()
                .flatMap { env -> env.services.map { env to it } }
                .flatMap { (env, svc) -> svc.instances.map { instance -> Triple(env, svc, instance) } }
                .filter { (env, svc, instance) -> svc.componentId == componentId }

        val services = inv.map { (env, svc, instance) -> svc }.associateBy { it.svc }

        val definition = inv
                .associateBy { (env, svc, instance) -> env.id to instance.id }
                .mapValues { (_, triple) -> triple.second to triple.third }
        val monitoredInstances = inventory.getEnvironments()
                .flatMap { env -> monitoring.instancesFor(env.id).map { env to it } }
        val monitoring = monitoredInstances
                .filter { (env, mon) -> mon.service?.let { services[it] }?.componentId == componentId }
                .associateBy { (env, mon) -> env.id to mon.instanceId }
                .mapValues { (_, pair) -> pair.second }

        return merge(definition, monitoring)
                .map { (id, instance, indicators) ->
                    val svc = instance?.first?.svc ?: indicators?.service ?: "x"
                    val service = services[svc] ?: Service(svc = svc, type = indicators?.type)
                    val component = service.componentId?.let { componentRepo.get(it) }

                    ExtInstance(id, component, service, instance?.second, indicators)
                }
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


