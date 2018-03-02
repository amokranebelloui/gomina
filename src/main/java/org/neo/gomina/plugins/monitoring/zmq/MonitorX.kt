package org.neo.gomina.plugins.monitoring.zmq

import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.plugins.monitoring.Monitoring
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
                val message = MessageParser.parse(obj)
                enrich(message.indicators)
                monitoring.notify(message.env, message.instanceId, message.indicators)
                logger.trace(message)
            }
            catch (e: Exception) {
                logger.error("", e)
            }
        }
        subscriber.close()
        context.term()
        logger.info("closed")
    }

    private fun enrich(indicators: MutableMap<String, String>) {
        indicators.put("TIMESTAMP", Date().toString()) // FIXME Date format
        indicators.put("status", mapStatus(indicators["status"]))
    }

    private fun mapStatus(status: String?) = if ("SHUTDOWN" == status) "DOWN" else status ?: "DOWN"

    companion object {
        private val logger = LogManager.getLogger(ZmqMonitorThread::class.java)
    }

}

data class Message(val env: String, val instanceId: String, val indicators: MutableMap<String, String>)

object MessageParser {
    fun parse(obj: String): Message {
        val i = obj.indexOf(";")
        val (_, _, env, instanceId) = obj.substring(0, i).split(".")
        return Message(env, instanceId, mapBody(obj.substring(i + 1)))
    }

    private fun mapBody(body: String): MutableMap<String, String> {
        return body.split(";")
                .filter { it.contains("=") }
                .map { keyValue -> val (key, value) = keyValue.split("="); Pair(key, value) }
                .toMap(mutableMapOf())
    }
}
