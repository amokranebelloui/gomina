package org.neo.gomina.plugins.monitoring

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.api.instances.InstanceDetail
import org.neo.gomina.api.instances.InstanceListener
import org.neo.gomina.api.instances.InstanceRealTime
import org.neo.gomina.integration.monitoring.Indicators
import org.neo.gomina.integration.monitoring.Monitoring
import org.neo.gomina.integration.zmqmonitoring.ZmqMonitorThreadPool
import org.neo.gomina.model.hosts.resolveHostname
import org.neo.gomina.model.inventory.Inventory
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

class MonitoringPlugin {

    @Inject lateinit var inventory: Inventory

    @Inject lateinit var monitoring: Monitoring
    @Inject lateinit var zmqThreadPool: ZmqMonitorThreadPool
    private val listeners = CopyOnWriteArrayList<InstanceListener>()

    fun registerListener(listener: InstanceListener) {
        this.listeners.add(listener)
    }

    // FIXME Specifics in one place

    fun prepare() {
        fun mapStatus(status: String?) = if ("SHUTDOWN" == status) "DOWN" else status ?: "DOWN"
        monitoring.enrich = { indicators ->
            indicators.put("TIMESTAMP", Date().toString())
            indicators["status"]?.let { status -> indicators.put("STATUS", mapStatus(status)) }
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

    fun init() {
        inventory.getEnvironments()
                .groupBy { it.monitoringUrl }
                .filterKeys { it != null }
                .forEach { (url, envs) -> zmqThreadPool.add(url!!, envs.map { ".#HB.${it.id}." }) }
        prepare()
    }

    companion object {
        private val logger = LogManager.getLogger(MonitoringPlugin::class.java)
    }
}

fun InstanceDetail.applyMonitoring(indicators: Indicators) {
    this.pid = indicators["PID"]
    this.host = resolveHostname(indicators["IP"])
    this.version = indicators["VERSION"]
    this.revision = indicators["REVISION"]
    this.unexpectedHost = StringUtils.isNotBlank(this.deployHost) && this.deployHost != this.host
    this.status = indicators["STATUS"]

    this.jmx = indicators["JMX"].asInt
    this.busVersion = indicators["BUS"]
    this.coreVersion = indicators["CORE"]
    this.quickfixPersistence = indicators["QUICKFIX_MODE"]
}

fun InstanceDetail.applyCluster(indicators: Indicators) {
    this.cluster = indicators["ELECTION"].asBoolean ?: false
    this.participating = indicators["PARTICIPATING"].asBoolean ?: false
    this.leader = indicators["LEADER"].asBoolean ?: true // Historically we didn't have this field
}

private fun InstanceRealTime.applyRealTime(newValues: Map<String, String>) {
    this.participating = newValues["PARTICIPATING"].asBoolean ?: false
    this.leader = newValues["LEADER"].asBoolean ?: true
    this.status = newValues["STATUS"]
}

fun InstanceDetail.applyRedis(indicators: Indicators) {
    this.redisHost = indicators["REDIS_HOST"]
    this.redisPort = indicators["REDIS_PORT"].asInt
    this.redisMasterHost = indicators["REDIS_MASTER_HOST"]
    this.redisMasterPort = indicators["REDIS_MASTER_PORT"].asInt
    this.redisMasterLink = "up" == indicators["REDIS_MASTER_LINK"]
    this.redisMasterLinkDownSince = indicators["REDIS_MASTER_LINK_DOWN_SINCE"]
    this.redisOffset = indicators["REDIS_OFFSET"].asLong
    this.redisOffsetDiff = indicators["REDIS_OFFSET_DIFF"].asLong
    this.redisMaster = indicators["REDIS_MASTER"].asBoolean
    this.redisRole = indicators["REDIS_ROLE"]
    this.redisRW = if ("yes".equals(indicators["REDIS_READONLY"], ignoreCase = true)) "ro" else "rw"
    this.redisMode = if ("1" == indicators["REDIS_AOF"]) "AOF" else "RDB"
    this.redisStatus = indicators["REDIS_STATE"]
    this.redisSlaveCount = indicators["REDIS_SLAVES"].asInt
    this.redisClientCount = indicators["REDIS_CLIENTS"].asInt
}

private fun String?.clean() = if (this == "null") null else this
private val String?.asInt: Int? get() = this.clean()?.toInt()
private val String?.asLong: Long? get() = this.clean()?.toLong()
private val String?.asBoolean: Boolean? get() = this.clean()?.toBoolean()


