package org.neo.gomina.model.monitoring.zmq

import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.monitoring.Monitoring
import org.zeromq.ZMQ
import java.util.*
import javax.inject.Inject

data class ZmqMonitorConfig (var connections: List<Connection> = emptyList())
data class Connection (var url: String)

class ZmqMonitorThreads {
    @Inject
    constructor(config: ZmqMonitorConfig, monitoring: Monitoring, inventory: Inventory) {
        if (config.connections != null) {
            val subscriptions = inventory.getEnvironments().map { ".#HB.${it.id}." }
            config.connections
                    .map { ZmqMonitorThread(monitoring, it.url, subscriptions) }
                    .forEach { it.start() }
        }
    }
}

class ZmqMonitorThread(private val monitoring: Monitoring, private val url: String, private val subscriptions: Collection<String>) : Thread() {

    override fun run() {
        val context = ZMQ.context(1)
        val subscriber = context.socket(ZMQ.SUB)
        subscriber.connect(url)
        for (subscription in subscriptions) {
            subscriber.subscribe(subscription.toByteArray())
        }
        logger.info("Listening to " + url)

        while (!Thread.currentThread().isInterrupted) {
            val obj = subscriber.recvStr(0)
            logger.trace("Received " + obj)

            try {
                val indicators = MessageParser.parse(obj)
                enrich(indicators)
                monitoring.notify(
                        indicators["@env"] as String,
                        indicators["@instanceId"] as String,
                        indicators)
                logger.trace(indicators)
            }
            catch (e: Exception) {
                logger.error("", e)
            }

        }
        subscriber.close()
        context.term()
        logger.info("closed")
    }

    private fun enrich(indicators: MutableMap<String, Any>) {
        indicators.put("TIMESTAMP", Date())
        indicators.put("STATUS", mapStatus(indicators["STATUS"] as String? ?: "DOWN"))
    }

    private fun mapStatus(status: String): String {
        return if ("SHUTDOWN" == status) "DOWN" else status
    }

    companion object {
        private val logger = LogManager.getLogger(ZmqMonitorThread::class.java)
    }

}

object MessageParser {
    fun parse(obj: String): MutableMap<String, Any> {
        val i = obj.indexOf(";")
        val (_, _, env, instanceId) = obj.substring(0, i).split(".")
        val indicators = mapBody(obj.substring(i + 1))
        indicators.put("@env", env)
        indicators.put("@instanceId", instanceId)
        return indicators
    }

    private fun mapBody(body: String): MutableMap<String, Any> {
        return body.split(";")
                .map { keyValue -> val (key, value) = keyValue.split("="); Pair(key, value) }
                .toMap(mutableMapOf())
    }
}
