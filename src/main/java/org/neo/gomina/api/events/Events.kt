package org.neo.gomina.api.events

import java.util.*

data class Event (
    val timestamp: Date,
    val type: String,
    val message: String?,
    
    val instanceId: String? = null,
    val version: String? = null
)

