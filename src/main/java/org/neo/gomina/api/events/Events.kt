package org.neo.gomina.api.events

import java.util.*

data class EventList(
        val events: List<EventDetail> = emptyList(),
        val errors: List<String> = emptyList()
)

data class EventDetail(
        val timestamp: Date,
        val type: String?,
        val message: String?,

        val envId: String? = null,
        val instanceId: String? = null,
        val version: String? = null
)

