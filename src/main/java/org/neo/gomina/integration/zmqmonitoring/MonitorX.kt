package org.neo.gomina.integration.zmqmonitoring

import org.apache.logging.log4j.LogManager
import org.zeromq.ZMQ
import java.util.*

data class ZmqMonitorConfig (var connections: List<Connection> = emptyList())
data class Connection (var url: String)

typealias MonitoringEventListener = (env: String, instanceId: String, newValues: Map<String, String>, touch: Boolean) -> Unit

class ZmqMonitorThread(private val listener: MonitoringEventListener, private val url: String, private val subscriptions: Collection<String>) : Thread() {

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
                if (message.indicators["STATUS"] != null && message.indicators["VERSION"] != null) { // FIXME Move out of generic
                    enrich(message.indicators)
                    listener(message.env, message.instanceId, message.indicators, true)
                }
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

    // FIXME Move out of generic
    private fun enrich(indicators: MutableMap<String, String>) {
        indicators.put("TIMESTAMP", Date().toString()) // FIXME Date format
        indicators.put("STATUS", mapStatus(indicators["STATUS"]))
    }

    // FIXME Move out of generic
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
