package org.neo.gomina.plugins.monitoring

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.neo.gomina.core.instances.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

class Indicators : ConcurrentHashMap<String, String>()

class EnvMonitoring {
    val instances: MutableMap<String, Indicators> = ConcurrentHashMap()
    fun getForInstance(name: String): Indicators {
        return instances.getOrPut(name) { Indicators() }
    }
}

class MonitoringPlugin : InstancesExt {

    private val topology = ConcurrentHashMap<String, EnvMonitoring>()

    private val listeners = CopyOnWriteArrayList<InstanceListener>()

    @Inject lateinit var instanceDetailRepository: InstanceDetailRepository

    fun registerListener(listener: InstanceListener) {
        this.listeners.add(listener)
    }

    override fun instancesInit() {

    }

    fun notify(env: String, instanceId: String, newValues: Map<String, String>) {
        try {
            val envMonitoring = topology.getOrPut(env) { EnvMonitoring() }
            val indicators = envMonitoring.getForInstance(instanceId)
            logger.trace("Notify $newValues")
            for ((key, value) in newValues) {
                if (value != null) indicators.put(key, value) else indicators.remove(key)
            }

            val id = env + "-" + instanceId // FIXME Only needed when returning all envs instances, simplify later
            val unexpected = Instance(id=id, env=env, type=indicators["TYPE"], service=indicators["SERVICE"], name=instanceId, unexpected = true)
            // FIXME Use a function
            val instance = instanceDetailRepository.getOrCreateInstance(id, unexpected)
            instance.applyMonitoring(indicators)

            val instanceRT = InstanceRealTime(env = env, id = instanceId, name = instanceId)
            instanceRT.applyRealTime(newValues)

            listeners.forEach { it.invoke(instanceRT) }
        }
        catch (e: Exception) {
            logger.error("Cannot notify env=$env instance=$instanceId", e)
        }
    }

    fun getFor(env: String): EnvMonitoring {
        return topology.getOrDefault(env, EnvMonitoring())
    }

    companion object {
        private val logger = LogManager.getLogger(MonitoringPlugin::class.java)
    }
}

fun Instance.applyMonitoring(indicators: Indicators) {
    this.pid = indicators["PID"]
    this.host = indicators["IP"]
    this.version = indicators["VERSION"]
    this.revision = indicators["REVISION"]

    applyCluster(indicators)

    this.status = indicators["STATUS"]
    this.jmx = indicators["JMX"].clean()?.toInt()
    this.busVersion = indicators["BUS"]
    this.coreVersion = indicators["CORE"]
    this.quickfixPersistence = indicators["QUICKFIX_MODE"]

    applyRedis(indicators)

    if (StringUtils.isNotBlank(this.deployHost) && this.deployHost != this.host) {
        this.unexpectedHost = true
    }
}

fun String?.clean() = if (this == "null") null else this

private fun Instance.applyCluster(indicators: Indicators) {
    this.cluster = indicators["ELECTION"]?.toBoolean() ?: false
    this.participating = indicators["PARTICIPATING"]?.toBoolean() ?: false
    this.leader = indicators["LEADER"]?.toBoolean() ?: true // Historically we didn't have this field
}

private fun InstanceRealTime.applyRealTime(newValues: Map<String, String>) {
    this.participating = newValues["PARTICIPATING"]?.toBoolean() ?: false
    this.leader = newValues["LEADER"]?.toBoolean() ?: true
    this.status = newValues["STATUS"]
}


private fun Instance.applyRedis(indicators: Indicators) {
    this.redisHost = indicators["REDIS_HOST"]
    this.redisPort = indicators["REDIS_PORT"].clean()?.toInt()
    this.redisMasterHost = indicators["REDIS_MASTER_HOST"]
    this.redisMasterPort = indicators["REDIS_MASTER_PORT"].clean()?.toInt()
    this.redisMasterLink = "up" == indicators["REDIS_MASTER_LINK"]
    this.redisMasterLinkDownSince = indicators["REDIS_MASTER_LINK_DOWN_SINCE"]
    this.redisOffset = indicators["REDIS_OFFSET"].clean()?.toLong()
    this.redisOffsetDiff = indicators["REDIS_OFFSET_DIFF"].clean()?.toLong()
    this.redisMaster = indicators["REDIS_MASTER"]?.toBoolean()
    this.redisRole = indicators["REDIS_ROLE"]
    this.redisRW = if ("yes".equals(indicators["REDIS_READONLY"], ignoreCase = true)) "ro" else "rw"
    this.redisMode = if ("1" == indicators["REDIS_AOF"]) "AOF" else "RDB"
    this.redisStatus = indicators["REDIS_STATE"]
    this.redisSlaveCount = indicators["REDIS_SLAVES"].clean()?.toInt()
    this.redisClientCount = indicators["REDIS_CLIENTS"].clean()?.toInt()
}

// FIXME Easier to have it on the UI level
private fun isLive(indicators: Map<String, Any>): Boolean {
    val timestamp = indicators["timestamp"] as LocalDateTime?  // FIXME Date format
    return if (timestamp != null) LocalDateTime(DateTimeZone.UTC).minusSeconds(1).isAfter(timestamp) else true
}

