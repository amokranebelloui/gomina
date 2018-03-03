package org.neo.gomina.plugins.monitoring

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.neo.gomina.core.instances.*
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

            val instance = InstanceRealTime(env = env, id = instanceId, name = instanceId)
            instance.applyRealTime(newValues)

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

fun Instance.applyMonitoring(indicators: Indicators) {
    this.pid = indicators["pid"]
    this.host = indicators["host"]
    this.version = indicators["version"]
    this.revision = indicators["revision"]

    applyCluster(indicators)

    this.status = indicators["status"]
    this.jmx = indicators["jmx"]?.toInt()
    this.busVersion = indicators["busVersion"]
    this.coreVersion = indicators["coreVersion"]
    this.quickfixPersistence = indicators["quickfixPersistence"]

    applyRedis(indicators)

    if (StringUtils.isNotBlank(this.deployHost) && this.deployHost != this.host) {
        this.unexpectedHost = true
    }
}

private fun Instance.applyCluster(indicators: Indicators) {
    this.cluster = indicators["cluster"]?.toBoolean() ?: false
    this.participating = indicators["participating"]?.toBoolean() ?: false
    this.leader = indicators["leader"]?.toBoolean() ?: true // Historically we didn't have this field
}

private fun InstanceRealTime.applyRealTime(newValues: Map<String, String>) {
    this.participating = newValues["participating"]?.toBoolean() ?: false
    this.leader = newValues["leader"]?.toBoolean() ?: true
    this.status = newValues["status"]
}


private fun Instance.applyRedis(indicators: Indicators) {
    this.redisHost = indicators["redisHost"]
    this.redisPort = indicators["redisPort"]?.toInt()
    this.redisMasterHost = indicators["redisMasterHost"]
    this.redisMasterPort = indicators["redisMasterPort"]?.toInt()
    this.redisMasterLink = indicators["redisMasterLink"]?.toBoolean()
    this.redisMasterLinkDownSince = indicators["redisMasterLinkDownSince"]
    this.redisOffset = indicators["redisOffset"]?.toInt()
    this.redisOffsetDiff = indicators["redisOffsetDiff"]?.toInt()
    this.redisMaster = indicators["redisMaster"]?.toBoolean()
    this.redisRole = indicators["redisRole"]
    this.redisRW = indicators["redisRW"]
    this.redisMode = indicators["redisMode"]
    this.redisStatus = indicators["redisStatus"]
    this.redisSlaveCount = indicators["redisSlaveCount"]?.toInt()
    this.redisClientCount = indicators["redisClientCount"]?.toInt()
}

// FIXME Easier to have it on the UI level
private fun isLive(indicators: Map<String, Any>): Boolean {
    val timestamp = indicators["timestamp"] as LocalDateTime?  // FIXME Date format
    return if (timestamp != null) LocalDateTime(DateTimeZone.UTC).minusSeconds(1).isAfter(timestamp) else true
}

