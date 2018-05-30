package org.neo.gomina.integration.monitoring

import org.apache.logging.log4j.LogManager
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.neo.gomina.integration.zmqmonitoring.ZmqMonitorConfig
import org.neo.gomina.plugins.monitoring.MonitoringPlugin
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.concurrent.thread

class Indicators(val instanceId: String, private val timeoutSeconds: Int = 5) : ConcurrentHashMap<String, String>() {
    private var lastTime = LocalDateTime(DateTimeZone.UTC)
    private var delayed = false
    fun touch() {
        lastTime = LocalDateTime(DateTimeZone.UTC)
        delayed = false
    }
    fun checkDelayed(notification: () -> Unit) {
        if (this.isLastTimeTooOld() && !delayed) {
            delayed = true
            notification()
        }

    }
    private fun isLastTimeTooOld(): Boolean {
        return LocalDateTime(DateTimeZone.UTC).isAfter(lastTime.plusSeconds(timeoutSeconds))
    }
}

class EnvMonitoring(private val timeoutSeconds: Int) {
    val instances: MutableMap<String, Indicators> = ConcurrentHashMap()
    fun getForInstance(name: String): Indicators {
        return instances.getOrPut(name) { Indicators(name, timeoutSeconds) }
    }
}

typealias MonitoringEventListener = (env: String, instanceId: String, newValues: Map<String, String>) -> Unit

class Monitoring {

    @Inject lateinit var config: ZmqMonitorConfig

    private val topology = ConcurrentHashMap<String, EnvMonitoring>()
    private var listener: MonitoringEventListener? = null
    private var delayMessageGenerator: () -> Map<String, String> = { emptyMap() }
    private var rtFields: Set<String> = emptySet()

    var include:(indicators: Map<String, String>) -> Boolean = { true }
    var enrich:(indicators: MutableMap<String, String>) -> Unit = {}

    fun checkFields(rtFields: Set<String>) {
        this.rtFields = rtFields
    }

    fun onDelay(delayMessageGenerator:() -> Map<String, String>) {
        this.delayMessageGenerator = delayMessageGenerator
    }

    fun onMessage(listener:MonitoringEventListener) {
        this.listener = listener
    }

    init {
        thread(start = true, name = "mon-ditcher") {
            while (!Thread.currentThread().isInterrupted) {
                topology.forEach { env, envMon ->
                    envMon.instances.forEach { instanceId, indicators ->
                        indicators.checkDelayed {
                            logger.info("Instance $env $instanceId delayed")
                            notify(env, instanceId, delayMessageGenerator().toMutableMap(), touch = false)
                        }
                    }
                }
                Thread.sleep(1000)
            }
        }
    }

    fun notify(env: String, instanceId: String, newValues: MutableMap<String, String>, touch: Boolean = true) {
        try {
            enrich(newValues)
            if (include(newValues)) {
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
                    listener?. let { listener -> listener(env, instanceId, newValues) }
                }
            }
        }
        catch (e: Exception) {
            logger.error("Cannot notify env=$env instance=$instanceId", e)
        }
    }

    fun instancesFor(envId: String): Collection<Indicators> {
        return topology[envId]?.instances?.values ?: emptyList()
    }
    
    companion object {
        private val logger = LogManager.getLogger(MonitoringPlugin::class.java)
    }
}