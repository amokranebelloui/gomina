package org.neo.gomina.integration.events

import org.apache.logging.log4j.LogManager
import org.neo.gomina.api.events.EventsProviderFactory
import org.neo.gomina.dummy.DummyEventsProviderConfig
import org.neo.gomina.integration.elasticsearch.ElasticEventsProviderConfig
import org.neo.gomina.integration.monitoring.MonitoringEventsProviderConfig
import org.neo.gomina.model.event.Events
import org.neo.gomina.model.event.EventsProvider
import org.neo.gomina.model.event.EventsProviderConfig
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

class EventsService {
    companion object {
        private val logger = LogManager.getLogger(EventsService.javaClass)
    }

    @JvmSuppressWildcards @Inject lateinit var config: List<EventsProviderConfig>
    @Inject lateinit var factory: EventsProviderFactory

    @Inject lateinit var events: Events

    lateinit var eventProviders: List<EventsProvider>

    @Inject
    fun init() {
        eventProviders = config.mapNotNull {
            when (it) {
                is MonitoringEventsProviderConfig -> factory.create(it)
                is ElasticEventsProviderConfig -> factory.create(it)
                is DummyEventsProviderConfig -> factory.create(it)
                else -> null
            }
        }
    }

    @Deprecated("Exposes remote repos", ReplaceWith("local repo directly"))
    fun eventProviders(): List<EventsProvider> {
        return eventProviders
    }

    fun reload() {
        logger.info("Reloading events into database")
        val since = LocalDate.now().minusDays(90).atStartOfDay(ZoneOffset.UTC).toLocalDateTime()
        eventProviders.forEach {
            try {
                logger.error("Reload events for '${it.name()}'")
                it.reload(since)
            }
            catch (e: Exception) {
                logger.error("Cannot reload events for '${it.name()}'", e)
            }
        }
    }

}