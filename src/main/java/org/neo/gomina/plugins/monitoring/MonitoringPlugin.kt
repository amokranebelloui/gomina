package org.neo.gomina.plugins.monitoring

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.instances.Instance
import org.neo.gomina.core.instances.InstanceListener
import org.neo.gomina.core.instances.InstanceRealTime
import org.neo.gomina.integration.monitoring.Indicators
import org.neo.gomina.integration.monitoring.Monitoring
import org.neo.gomina.integration.zmqmonitoring.ZmqMonitorConfig
import org.neo.gomina.integration.zmqmonitoring.ZmqMonitorThread
import org.neo.gomina.model.hosts.resolveHostname
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.plugins.Plugin
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

class MonitoringPlugin : Plugin {

    @Inject lateinit var config: ZmqMonitorConfig
    @Inject lateinit var inventory: Inventory

    @Inject lateinit var monitoring: Monitoring
    private val listeners = CopyOnWriteArrayList<InstanceListener>()

    fun registerListener(listener: InstanceListener) {
        this.listeners.add(listener)
    }

    // FIXME Specifics in one place

    fun prepare() {
        monitoring.enrich = { indicators ->
            indicators.put("TIMESTAMP", Date().toString())
            indicators["status"]?.let { status -> indicators.put("STATUS", this.mapStatus(status)) }
        }
        monitoring.include = { it["STATUS"] != null && it["VERSION"] != null }
        monitoring.checkFields(setOf("PARTICIPATING", "LEADER", "STATUS"))
        monitoring.onMessage { env, instanceId, newValues ->
            val instanceRT = InstanceRealTime(env = env, id = instanceId, name = instanceId)
            instanceRT.applyRealTime(newValues)
            listeners.forEach { it.invoke(instanceRT) }
        }
        monitoring.onDelay {
            mapOf("STATUS" to "NOINFO")
        }
    }

    override fun init() {
        if (config.connections != null) {
            val subscriptions = inventory.getEnvironments().map { ".#HB.${it.id}." }
            config.connections
                    .map { ZmqMonitorThread(monitoring, it.url, subscriptions) }
                    .forEach { it.start() }
        }
        prepare()
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


