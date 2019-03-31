package org.neo.gomina.plugins

import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.zmqmonitoring.ZmqMonitorThreadPool
import org.neo.gomina.model.inventory.Inventory
import javax.inject.Inject
import kotlin.concurrent.timer

class PluginAssembler {

    @Inject lateinit var inventory: Inventory
    @Inject lateinit var zmqThreadPool: ZmqMonitorThreadPool

    fun init() {
        timer(name = "monitoring-checker", period = 5000) {
            logger.debug("Checking if any new env to monitor ...")
            inventory.getEnvironments()
                    .mapNotNull { it.monitoringUrl }
                    .map {
                        val sep = it.lastIndexOf('@')
                        if (sep != -1) it.substring(0, sep) to it.substring(sep + 1) else it to null
                    }
                    .groupBy { (url, env) -> url }
                    .forEach { (url, envs) -> zmqThreadPool.add(url, envs.map { (url, env) -> ".#HB.$env." }) }
        }
    }

    companion object {
        private val logger = LogManager.getLogger(PluginAssembler::class.java)
    }

}
