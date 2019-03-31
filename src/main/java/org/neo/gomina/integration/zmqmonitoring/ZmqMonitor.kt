package org.neo.gomina.integration.zmqmonitoring

import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.monitoring.Monitoring
import org.neo.gomina.model.monitoring.RuntimeInfo
import org.zeromq.ZMQ
import javax.inject.Inject

interface MonitoringMapper {
    fun map(instanceId: String, indicators: Map<String, String>): RuntimeInfo?
}

class ZmqMonitorThreadPool {
    companion object {
        private val logger = LogManager.getLogger(ZmqMonitorThreadPool::class.java)
    }

    @Inject lateinit var monitoring: Monitoring
    @Inject lateinit var monitoringMapper: MonitoringMapper
    val map = mutableMapOf<String, ZmqMonitorThread>()

    fun add(url: String, subscriptions: Collection<String>) {
        val thread = map.getOrPut(url) {
            logger.info("Monitoring $url w/ $subscriptions")
            ZmqMonitorThread(monitoring, url, monitoringMapper).apply { start() }
        }
        subscriptions.forEach { thread.subscribe(it) }
    }
}

class ZmqMonitorThread(
        private val monitoring: Monitoring,
        private val url: String,
        private val monitoringMapper: MonitoringMapper
    ) : Thread() {

    private val subscriptions = mutableSetOf<String>()
    private val context: ZMQ.Context = ZMQ.context(1)
    private val subscriber: ZMQ.Socket

    init {
        subscriber = context.socket(ZMQ.SUB)
        try {
            subscriber.connect(url)
            logger.info("Listening on $url")
        }
        catch (e: Exception) {
            logger.info("Error listening on $url", e.message)
        }
    }

    fun subscribe(subscription: String) {
        if (subscriptions.add(subscription)) {
            subscriber.subscribe(subscription.toByteArray())
            logger.info("Subscribed to $subscription, on $url")
        }
    }

    override fun run() {
        while (!Thread.currentThread().isInterrupted) {
            val obj = subscriber.recvStr(0)
            logger.trace("Received " + obj)
            try {
                val message = MessageParser.parse(obj)
                val info = monitoringMapper.map(message.instanceId, message.indicators)
                info?.let {
                    monitoring.notify(message.env, message.instanceId, info, touch = true)
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

    companion object {
        private val logger = LogManager.getLogger(ZmqMonitorThread::class.java)
    }

}

internal data class Message(val env: String, val instanceId: String, val indicators: MutableMap<String, String>)

internal object MessageParser {
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
