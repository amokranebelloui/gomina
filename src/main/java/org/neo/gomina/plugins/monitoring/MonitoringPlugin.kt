package org.neo.gomina.plugins.monitoring

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.instances.Instance
import org.neo.gomina.core.instances.InstanceListener
import org.neo.gomina.core.instances.InstanceRealTime
import org.neo.gomina.integration.monitoring.EnvMonitoring
import org.neo.gomina.integration.monitoring.Indicators
import org.neo.gomina.integration.zmqmonitoring.ZmqMonitorConfig
import org.neo.gomina.integration.zmqmonitoring.ZmqMonitorThread
import org.neo.gomina.model.hosts.resolveHostname
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.plugins.Plugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import kotlin.concurrent.thread

class MonitoringPlugin : Plugin {

    @Inject lateinit var config: ZmqMonitorConfig
    @Inject lateinit var inventory: Inventory

    private val topology = ConcurrentHashMap<String, EnvMonitoring>()
    private val listeners = CopyOnWriteArrayList<InstanceListener>()

    private val rtFields = setOf("PARTICIPATING", "LEADER", "STATUS")

    fun registerListener(listener: InstanceListener) {
        this.listeners.add(listener)
    }

    override fun init() {
        if (config.connections != null) {
            val subscriptions = inventory.getEnvironments().map { ".#HB.${it.id}." }
            config.connections
                    .map { ZmqMonitorThread(this::notify, it.url, subscriptions, this::include, this::enrich) }
                    .forEach { it.start() }
        }
        thread(start = true, name = "mon-ditcher") {
            while (!Thread.currentThread().isInterrupted) {
                topology.forEach { env, envMon ->
                    envMon.instances.forEach { instanceId, indicators ->
                        indicators.checkDelayed {
                            logger.info("Instance $env $instanceId delayed")
                            notify(env, instanceId, mapOf("STATUS" to "NOINFO"), touch = false) // FIXME Specifics in one place
                        }
                    }
                }
                Thread.sleep(1000)
            }
        }
    }

    fun instancesFor(envId: String): Collection<Indicators> {
        return topology[envId]?.instances?.values ?: emptyList()
    }

    fun notify(env: String, instanceId: String, newValues: Map<String, String>, touch: Boolean = true) {
        try {
            val envMonitoring = topology.getOrPut(env) { EnvMonitoring(config.timeoutSeconds) }
            val indicators = envMonitoring.getForInstance(instanceId)
            if (touch) {
                indicators.touch()
            }
            logger.trace("Notify $newValues")
            var rt = false
            for ((key, value) in newValues) {
                if (rtFields.contains(key)) {
                    val oldValue = indicators[key]
                    rt = rt || oldValue != value
                }
                if (value != null) indicators.put(key, value) else indicators.remove(key)
            }

            if (rt) {
                val instanceRT = InstanceRealTime(env = env, id = instanceId, name = instanceId)
                instanceRT.applyRealTime(newValues)
                listeners.forEach { it.invoke(instanceRT) }
            }
        }
        catch (e: Exception) {
            logger.error("Cannot notify env=$env instance=$instanceId", e)
        }
    }

    // FIXME Specifics in one place
    fun include(indicators: MutableMap<String, String>) = indicators["STATUS"] != null && indicators["VERSION"] != null

    fun enrich(indicators: MutableMap<String, String>) {
        indicators.put("TIMESTAMP", Date().toString()) // FIXME Date format
        indicators["status"]?.let { indicators.put("STATUS", mapStatus(it)) }
    }

    private fun mapStatus(status: String?) = if ("SHUTDOWN" == status) "DOWN" else status ?: "DOWN"

    companion object {
        private val logger = LogManager.getLogger(MonitoringPlugin::class.java)
    }
}

fun Instance.applyMonitoring(indicators: Indicators) {
    this.pid = indicators["PID"]
    this.host = resolveHostname(indicators["IP"])
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


