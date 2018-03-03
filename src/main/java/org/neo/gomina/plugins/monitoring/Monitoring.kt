package org.neo.gomina.plugins.monitoring

import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.instances.InstanceRealTime
import org.neo.gomina.core.instances.Instances
import org.neo.gomina.core.instances.InstancesExt
import org.neo.gomina.core.instances.InstanceListener
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class Indicators : ConcurrentHashMap<String, String>()

class EnvMonitoring {
    val instances: MutableMap<String, Indicators> = ConcurrentHashMap()
    fun getForInstance(name: String): MutableMap<String, String> {
        return instances.getOrPut(name) { Indicators() }
    }
}

class Monitoring : InstancesExt {

    private val topology = ConcurrentHashMap<String, EnvMonitoring>()

    private val listeners = CopyOnWriteArrayList<InstanceListener>()

    override fun onRegisterForInstanceUpdates(listener: InstanceListener) {
        this.listeners.add(listener)
    }

    override fun onGetInstances(env: String, instances: Instances) {
        val monitoring = this.getFor(env)
        for ((instanceId, indicators) in monitoring.instances) {
            val id = env + "-" + instanceId // FIXME Only needed when returning all envs instances, simplify later
            var instance = instances.ensure(id, env, indicators["type"], indicators["service"], instanceId, expected = false)
            instance.applyMonitoring(indicators)
        }
    }

    fun notify(env: String, instanceId: String, newValues: Map<String, String>) {
        try {
            val envMonitoring = topology.getOrPut(env) { EnvMonitoring() }
            val indicators = envMonitoring.getForInstance(instanceId)
            logger.trace("Notify $newValues")
            for ((key, value) in newValues) {
                if (value != null) indicators.put(key, value) else indicators.remove(key)
            }

            val instance = InstanceRealTime(
                    env = env,
                    id = instanceId,
                    name = instanceId,
                    participating = newValues["participating"]?.toBoolean() ?: false,
                    leader = newValues["leader"]?.toBoolean() ?: true,
                    status = newValues["status"]
            )

            listeners.forEach { it.invoke(instance) }
        }
        catch (e: Exception) {
            logger.error("Cannot notify env=$env instance=$instanceId", e)
        }
    }

    fun getFor(env: String): EnvMonitoring {
        return topology.getOrDefault(env, EnvMonitoring())
    }

    companion object {
        private val logger = LogManager.getLogger(Monitoring::class.java)
    }
}
