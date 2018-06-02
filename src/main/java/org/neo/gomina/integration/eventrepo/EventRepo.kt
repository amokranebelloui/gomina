package org.neo.gomina.integration.eventrepo

import org.neo.gomina.model.event.Event
import org.neo.gomina.model.event.EventsProvider
import java.time.LocalDateTime

class EventRepo : EventsProvider {

    override fun getEvents(since: LocalDateTime): List<Event> {
        // FIXME Dummy Data
        return listOf(
                Event(LocalDateTime.of(2018, 6, 1, 18, 57, 0), "release", "Release order manager", instanceId = "order", version = "2.5.6"),
                Event(LocalDateTime.of(2018, 6, 1, 18, 55, 0), "maintenance", "Purge data"),
                Event(LocalDateTime.of(2018, 6, 2, 1, 12, 0), "outage", "database trading down"),
                Event(LocalDateTime.of(2018, 4, 20, 19, 0, 0), "release", "Release bugfix", instanceId = "basket", version = "1.1.5")
        )
        .filter { it.timestamp > since }
    }

}