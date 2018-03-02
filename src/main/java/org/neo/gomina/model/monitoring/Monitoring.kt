package org.neo.gomina.model.monitoring

import org.apache.logging.log4j.LogManager
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class Indicators : ConcurrentHashMap<String, String>()

class EnvMonitoring {
    val instances: MutableMap<String, Indicators> = ConcurrentHashMap()
    fun getForInstance(name: String): MutableMap<String, String> {
        return instances.getOrPut(name) { Indicators() }
    }
}

typealias MonitoringListener = (env: String, instanceId: String, indicators: Map<String, String>) -> Unit

class Monitoring {

    private val topology = ConcurrentHashMap<String, EnvMonitoring>()

    private val listeners = CopyOnWriteArrayList<MonitoringListener>()

    fun add(listener: MonitoringListener) {
        this.listeners.add(listener)
    }

    fun notify(env: String, instanceId: String, newValues: Map<String, String>) {
        try {
            val envMonitoring = topology.getOrPut(env) { EnvMonitoring() }
            val indicators = envMonitoring.getForInstance(instanceId)
            logger.trace("Notify $newValues")
            for ((key, value) in newValues) {
                if (value != null) indicators.put(key, value) else indicators.remove(key)
            }

            listeners.forEach { it.invoke(env, instanceId, newValues) }
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
