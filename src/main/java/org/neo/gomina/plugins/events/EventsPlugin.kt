package org.neo.gomina.plugins.events

import com.google.inject.name.Named
import org.neo.gomina.integration.elasticsearch.ElasticEvents
import org.neo.gomina.integration.eventrepo.EventRepo
import javax.inject.Inject

class EventsPlugin {

    @Inject lateinit var eventRepo: EventRepo
    @Inject @Named("releases") lateinit var releaseEvents: ElasticEvents

    fun eventProviders() = listOf(eventRepo, releaseEvents)
    
}