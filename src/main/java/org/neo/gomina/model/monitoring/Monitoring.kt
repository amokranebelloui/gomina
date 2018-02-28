package org.neo.gomina.model.monitoring

import org.apache.logging.log4j.LogManager
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList


class EnvMonitoring {

    // instance.id / indicators
    private var map: MutableMap<String, MutableMap<String, Any>> = ConcurrentHashMap()

    fun getAll(): Map<String, Map<String, Any>> = map

    fun getForInstance(name: String): MutableMap<String, Any> {
        return map.getOrPut(name) { ConcurrentHashMap() }
    }
}

class Monitoring {

    private val topology = ConcurrentHashMap<String, EnvMonitoring>()

    private val listeners = CopyOnWriteArrayList<MonitoringListener>()

    fun add(listener: MonitoringListener) {
        this.listeners.add(listener)
    }

    fun notify(env: String, instanceId: String, newValues: Map<String, Any>) {
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
            logger.error("Cannot notify env={} instance={}", env, instanceId, e)
        }
    }

    fun getFor(env: String): EnvMonitoring {
        return topology.getOrDefault(env, EnvMonitoring())
    }

    companion object {
        private val logger = LogManager.getLogger(Monitoring::class.java)
    }
}

/*
interface MonitoringListener {
    fun onPropertyChanged(env: String, instanceId: String, newValues: Map<String, Any>)
}
*/

typealias MonitoringListener = (String, String, Map<String,Any>) -> Unit