package org.neo.gomina.integration.eventrepo

import org.neo.gomina.model.event.Event
import org.neo.gomina.model.event.EventsProvider
import org.neo.gomina.model.event.EventsProviderConfig
import java.time.LocalDate
import java.time.LocalDateTime

data class InternalEventsProviderConfig(
        var id: String
) : EventsProviderConfig

class InternalEventsProvider : EventsProvider {

    override fun name(): String = "internal"

    override fun events(since: LocalDateTime): List<Event> {
        // FIXME Dummy Data

        return listOf(
                Event(LocalDate.now().atStartOfDay(), "info", "Greeting for today"),
                Event(LocalDateTime.of(2018, 6, 1, 18, 55, 0), "maintenance", "Purge data (DUMMY)"),
                Event(LocalDateTime.of(2018, 6, 2, 1, 12, 0), "outage", "database trading down (DUMMY)"),
                Event(LocalDateTime.of(2018, 4, 20, 19, 0, 0), "release", "Release bugfix (DUMMY)", instanceId = "basket", version = "1.1.5")
        )
        .filter { it.timestamp > since }
    }

}