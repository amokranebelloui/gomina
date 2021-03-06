package org.neo.gomina.integration.monitoring

import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.event.*
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.monitoring.Monitoring
import org.neo.gomina.model.monitoring.ServerStatus
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject

data class MonitoringEventsProviderConfig(
        var id: String
) : EventsProviderConfig

class MonitoringEventsProvider : EventsProvider {

    companion object {
        private val logger = LogManager.getLogger(javaClass)
    }

    @Inject private lateinit var monitoring: Monitoring
    @Inject private lateinit var inventory: Inventory
    @Inject private lateinit var components: ComponentRepo
    @Inject private lateinit var events: Events

    @Inject
    fun init() {
        val now = LocalDateTime.now(Clock.systemUTC())
        val id = "$now-server-start"
        events.save(listOf(Event(id, now, group = EventCategory.INFO, type = "info", message = "Server Started", global = true)))
        monitoring.onMessage { env, service, instanceId, oldValues, newValues ->
            oldValues?.let {
                val newS = newValues.process.status
                val oldS = oldValues.process.status
                val change = when {
                    newS == ServerStatus.LIVE && oldS == ServerStatus.OFFLINE -> "online" to "$instanceId back online"
                    newS == ServerStatus.LIVE && oldS != ServerStatus.LIVE -> "started" to "$instanceId started"
                    newS == ServerStatus.DOWN && oldS == ServerStatus.LIVE -> "stopped" to "$instanceId stopped"
                    newS == ServerStatus.LOADING && oldS != ServerStatus.LOADING -> "loading" to "$instanceId loading"
                    newS == ServerStatus.OFFLINE && oldS != ServerStatus.OFFLINE -> "offline" to "$instanceId offline"
                    newS != ServerStatus.LIVE && oldS == ServerStatus.LIVE -> "runtime" to "$instanceId status changed to $newS"
                    else -> null
                }
                if (newS != oldS) {
                    logger.info("Status Change $oldS->$newS => $change")
                }
                change?.let { (type, message) ->
                    val timestamp = LocalDateTime.now(Clock.systemUTC())
                    val id = "$timestamp-$env-$instanceId"
                    val componentId = inventory.getEnvironment(env)
                            ?.services?.find { it.svc == service }
                            ?.componentId
                    events.save(listOf(Event(id, timestamp, group = EventCategory.RUNTIME, type = type, message = message,
                            envId = env, instanceId = instanceId, componentId = componentId)))
                }
            }
        }
    }

    override fun name(): String = "internal"

    override fun reload(since: LocalDateTime) {

    }

}