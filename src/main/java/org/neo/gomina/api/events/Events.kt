package org.neo.gomina.api.events

import java.util.*

data class EventDetail(
        val timestamp: Date,
        val type: String?,
        val message: String?,

        val envId: String? = null,
        val instanceId: String? = null,
        val version: String? = null
)

