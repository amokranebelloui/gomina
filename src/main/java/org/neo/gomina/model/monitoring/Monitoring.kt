package org.neo.gomina.model.monitoring

import com.google.inject.name.Named
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.inventory.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import kotlin.concurrent.thread

private fun String?.clean() = if (this == "null") null else this
val String?.asString: String? get() = this.clean()
val String?.asInt: Int? get() = this.clean()?.toInt()
val String?.asLong: Long? get() = this.clean()?.toLong()
val String?.asBoolean: Boolean? get() = this.clean()?.toBoolean()
val String?.asTime: LocalDateTime? get() = this.clean()?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) }

typealias MonitoringEventListener = (env: String, service: String?, instanceId: String, oldValues: RuntimeInfo?, newValues: RuntimeInfo) -> Unit

class Monitoring {

    @Inject @Named("monitoring.timeout") var timeoutSeconds: Int = 5

    private val topology = ConcurrentHashMap<String, MutableMap<String, RuntimeInfo>>()
    //private var listener: MonitoringEventListener? = null
    private val listeners = CopyOnWriteArrayList<MonitoringEventListener>() // FIXME List rather than just 1 listener

    private var fieldsChanged: (a: RuntimeInfo, b: RuntimeInfo) -> Boolean = { _, _ -> false }

    fun fieldsChanged(fieldsChanged: (a: RuntimeInfo, b:RuntimeInfo) -> Boolean) {
        this.fieldsChanged = fieldsChanged
    }

    fun onMessage(listener: MonitoringEventListener) {
        this.listeners.add(listener)
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
                            notify(env, indicators.service, instanceId, delay, touch = false)
                        }
                    }
                }
                Thread.sleep(1000)
            }
        }
    }

    fun notify(env: String, service: String?, instanceId: String, newValues: RuntimeInfo, touch: Boolean = true) {
        try {
            val envMonitoring = topology.getOrPut(env) { ConcurrentHashMap() }
            val oldValues = envMonitoring[instanceId]
            /*
            if (touch) {
                oldValues.touch()
            }
            */
            logger.trace("Notify $newValues")
            var rt = oldValues != null && fieldsChanged(oldValues, newValues)
            /*
            for ((key, value) in newValues) {
                if (rtFields.contains(key)) {
                    val oldValue = oldValues[key]
                    rt = rt || oldValue != value
                }
                if (value != null) oldValues.put(key, value) else oldValues.remove(key)
            }
            */
            envMonitoring.put(instanceId, newValues)

            if (rt) {
                listeners.forEach { listener -> listener(env, service, instanceId, oldValues, newValues) }
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