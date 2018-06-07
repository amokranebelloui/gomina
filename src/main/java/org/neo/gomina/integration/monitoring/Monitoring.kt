package org.neo.gomina.integration.monitoring

import com.google.inject.name.Named
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.monitoring.RuntimeInfo
import org.neo.gomina.model.monitoring.ServerStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.concurrent.thread

private fun String?.clean() = if (this == "null") null else this
val String?.asInt: Int? get() = this.clean()?.toInt()
val String?.asLong: Long? get() = this.clean()?.toLong()
val String?.asBoolean: Boolean? get() = this.clean()?.toBoolean()
val String?.asTime: LocalDateTime? get() = this.clean()?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) }

typealias MonitoringEventListener = (env: String, instanceId: String, newValues: RuntimeInfo) -> Unit

class Monitoring {

    @Inject @Named("monitoring.timeout") var timeoutSeconds: Int = 5

    private val topology = ConcurrentHashMap<String, MutableMap<String, RuntimeInfo>>()
    private var listener: MonitoringEventListener? = null
    //private val listeners = CopyOnWriteArrayList<MonitoringEventListener>() // FIXME List rather than just 1 listener

    private var fieldsChanged: (a: RuntimeInfo, b: RuntimeInfo) -> Boolean = { _, _ -> false }

    fun fieldsChanged(fieldsChanged: (a: RuntimeInfo, b:RuntimeInfo) -> Boolean) {
        this.fieldsChanged = fieldsChanged
    }

    fun onMessage(listener:MonitoringEventListener) {
        this.listener = listener
    }

    init {
        thread(start = true, name = "mon-ditcher") {
            while (!Thread.currentThread().isInterrupted) {
                topology.forEach { env, envMon ->
                    envMon.forEach { instanceId, indicators ->
                        indicators.checkDelayed(timeoutSeconds) {
                            logger.info("Instance $env $instanceId delayed")
                            val delay = indicators.copy(
                                    delayed = true,
                                    process = indicators.process.copy(status = ServerStatus.OFFLINE)
                            )
                            notify(env, instanceId, delay, touch = false)
                        }
                    }
                }
                Thread.sleep(1000)
            }
        }
    }

    fun notify(env: String, instanceId: String, newValues: RuntimeInfo, touch: Boolean = true) {
        try {
            val envMonitoring = topology.getOrPut(env) { ConcurrentHashMap() }
            val indicators = envMonitoring[instanceId]
            /*
            if (touch) {
                indicators.touch()
            }
            */
            logger.trace("Notify $newValues")
            var rt = indicators != null && fieldsChanged(indicators, newValues)
            /*
            for ((key, value) in newValues) {
                if (rtFields.contains(key)) {
                    val oldValue = indicators[key]
                    rt = rt || oldValue != value
                }
                if (value != null) indicators.put(key, value) else indicators.remove(key)
            }
            */
            envMonitoring.put(instanceId, newValues)

            if (rt) {
                listener?. let { listener -> listener(env, instanceId, newValues) }
            }
        }
        catch (e: Exception) {
            logger.error("Cannot notify env=$env instance=$instanceId", e)
        }
    }

    fun instancesFor(envId: String): Collection<RuntimeInfo> {
        return topology[envId]?.values ?: emptyList()
    }
    
    companion object {
        private val logger = LogManager.getLogger(Monitoring::class.java)
    }
}