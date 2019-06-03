package org.neo.gomina.model.event

import java.time.LocalDateTime

enum class EventCategory {RUNTIME, RELEASE, VERSION, INFO}

data class Event (
        val id: String,
        val timestamp: LocalDateTime,
        val group: EventCategory,
        val type: String?,
        val message: String?,
        // Optional metadata
        val global: Boolean = false,
        val envId: String? = null,
        val instanceId: String? = null,
        val componentId: String? = null,
        val version: String? = null
)

interface Events {
    fun forEnv(envId: String): List<Event>
    fun forComponent(componentId: String): List<Event>
    fun releases(componentId: String, prodEnvs: List<String>): List<Event>
    fun save(events: List<Event>)
}

interface EventsProviderConfig

interface EventsProvider {
    fun name(): String
    fun reload(since: LocalDateTime)
}

