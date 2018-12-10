package org.neo.gomina.integration.monitoring

import org.neo.gomina.model.event.Event
import org.neo.gomina.model.event.EventsProvider
import org.neo.gomina.model.event.EventsProviderConfig
import org.neo.gomina.model.monitoring.Monitoring
import org.neo.gomina.model.monitoring.ServerStatus
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

data class MonitoringEventsProviderConfig(
        var id: String
) : EventsProviderConfig

class MonitoringEventsProvider : EventsProvider {

    @Inject private lateinit var monitoring: Monitoring

    private var seq = 1
    private val events = mutableListOf<Event>()

    @Inject
    fun init() {
        monitoring.onMessage { env, instanceId, oldValues, newValues ->
            oldValues?.let {
                val newStatus = newValues.process.status
                val oldStatus = oldValues.process.status
                if (newStatus == ServerStatus.LIVE && oldStatus != ServerStatus.LIVE) {
                    events.add(Event("${seq++}", LocalDateTime.now(Clock.systemUTC()), type = "runtime", message = "$instanceId started"))
                }
                if (newStatus == ServerStatus.DOWN && oldStatus == ServerStatus.LIVE) {
                    events.add(Event("${seq++}", LocalDateTime.now(Clock.systemUTC()), type = "runtime", message = "$instanceId stopped"))
                }
                if (newStatus != ServerStatus.LIVE && oldStatus == ServerStatus.LIVE) {
                    events.add(Event("${seq++}", LocalDateTime.now(Clock.systemUTC()), type = "runtime", message = "$instanceId status changed to $newStatus"))
                }
            }
        }
    }

    override fun name(): String = "internal"

    override fun events(since: LocalDateTime): List<Event> {
        // FIXME Dummy Data

        return (events + Event("${seq++}", LocalDate.now().atStartOfDay(), "info", "Greeting for today"))
            .filter { it.timestamp > since }
    }

}