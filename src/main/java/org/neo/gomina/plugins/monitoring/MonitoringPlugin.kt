package org.neo.gomina.plugins.monitoring

import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.monitoring.Monitoring
import org.neo.gomina.integration.zmqmonitoring.ZmqMonitorThreadPool
import org.neo.gomina.model.inventory.Inventory
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.timer

class MonitoringPlugin {

    @Inject lateinit var inventory: Inventory

    @Inject lateinit var monitoring: Monitoring
    @Inject lateinit var zmqThreadPool: ZmqMonitorThreadPool

    // FIXME Specifics in one place

    fun prepare() {
        fun mapStatus(status: String?) = if ("SHUTDOWN" == status) "DOWN" else status ?: "DOWN"
        monitoring.enrich = { indicators ->
            indicators.put("TIMESTAMP", Date().toString())
            indicators["status"]?.let { status -> indicators.put("STATUS", mapStatus(status)) }
        }
        monitoring.include = { it["STATUS"] != null && it["VERSION"] != null }
        monitoring.checkFields(setOf("PARTICIPATING", "LEADER", "STATUS"))
        monitoring.onDelay {
            mapOf("STATUS" to "NOINFO")
        }
    }

    fun init() {
        timer(name = "monitoring-checker", period = 5000) {
            logger.debug("Checking if any new env to monitor ...")
            inventory.getEnvironments()
                    .groupBy { it.monitoringUrl }
                    .filterKeys { it != null }
                    .forEach { (url, envs) -> zmqThreadPool.add(url!!, envs.map { ".#HB.${it.id}." }) }
        }
    }

    companion object {
        private val logger = LogManager.getLogger(MonitoringPlugin::class.java)
    }
}



