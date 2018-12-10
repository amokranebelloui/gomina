package org.neo.gomina.model.event

import java.time.LocalDateTime

data class Event (
        val id: String,
        val timestamp: LocalDateTime,
        val type: String?,
        val message: String?,
        // Optional metadata
        val envId: String? = null,
        val instanceId: String? = null,
        val version: String? = null
)

interface EventsProviderConfig

interface EventsProvider {
    fun name(): String
    fun events(since: LocalDateTime): List<Event>
}

