package org.neo.gomina.integration.monitoring

import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.event.Event
import org.neo.gomina.model.event.EventsProvider
import org.neo.gomina.model.event.EventsProviderConfig
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

    @Inject private lateinit var monitoring: Monitoring
    @Inject private lateinit var inventory: Inventory
    @Inject private lateinit var components: ComponentRepo

    private val events = mutableListOf<Event>()

    @Inject
    fun init() {
        val now = LocalDateTime.now(Clock.systemUTC())
        val id = "$now-server-start"
        events.add(Event(id, now, type = "info", message = "Server Started"))
        monitoring.onMessage { env, service, instanceId, oldValues, newValues ->
            oldValues?.let {
                val newS = newValues.process.status
                val oldS = oldValues.process.status
                val message = when {
                    newS == ServerStatus.LIVE && oldS != ServerStatus.LIVE -> "$instanceId started"
                    newS == ServerStatus.DOWN && oldS == ServerStatus.LIVE -> "$instanceId stopped"
                    newS != ServerStatus.LIVE && oldS == ServerStatus.LIVE -> "$instanceId status changed to $newS"
                    else -> null
                }
                message?.let {
                    val timestamp = LocalDateTime.now(Clock.systemUTC())
                    val id = "$timestamp-$env-$instanceId"
                    val componentId = inventory.getEnvironment(env)
                            ?.services?.find { it.svc == service }
                            ?.componentId
                    events.add(Event(id, timestamp, type = "runtime", message = message,
                            envId = env, instanceId = instanceId, componentId = componentId))
                }
            }
        }
    }

    override fun name(): String = "internal"

    override fun events(since: LocalDateTime): List<Event> {
        return events.filter { it.timestamp > since }
    }

}