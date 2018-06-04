package org.neo.gomina.plugins.events

import org.neo.gomina.integration.elasticsearch.ElasticEvents
import org.neo.gomina.integration.elasticsearch.ElasticEventsProviderConfig
import org.neo.gomina.integration.eventrepo.InternalEvents
import org.neo.gomina.integration.eventrepo.InternalEventsProviderConfig
import org.neo.gomina.model.event.EventsProvider
import org.neo.gomina.model.event.EventsProviderConfig
import javax.inject.Inject

interface EventsProviderFactory {
    fun create(config: InternalEventsProviderConfig): InternalEvents
    fun create(config: ElasticEventsProviderConfig): ElasticEvents
}

class EventsPlugin {

    @Inject lateinit var factory: EventsProviderFactory

    @JvmSuppressWildcards @Inject lateinit var config: List<EventsProviderConfig>

    fun eventProviders(): List<EventsProvider> {
        return config.mapNotNull {
            when (it) {
                is InternalEventsProviderConfig -> factory.create(it)
                is ElasticEventsProviderConfig -> factory.create(it)
                else -> null
            }
        }
    }

}