package org.neo.gomina.api.events

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.api.toDateUtc
import org.neo.gomina.integration.elasticsearch.ElasticEventsProvider
import org.neo.gomina.integration.elasticsearch.ElasticEventsProviderConfig
import org.neo.gomina.integration.eventrepo.InternalEventsProvider
import org.neo.gomina.integration.eventrepo.InternalEventsProviderConfig
import org.neo.gomina.model.event.Event
import org.neo.gomina.model.event.EventsProvider
import org.neo.gomina.model.event.EventsProviderConfig
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

class EventsApi {

    companion object {
        private val logger = LogManager.getLogger(EventsApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject lateinit var factory: EventsProviderFactory
    @JvmSuppressWildcards @Inject lateinit var config: List<EventsProviderConfig>

    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.get("/:envId").handler(this::eventsForEnv)
    }

    private fun eventsForEnv(ctx: RoutingContext) {
        try {
            val envId = ctx.request().getParam("envId")
            logger.info("Events for $envId")
            val since = LocalDate.now().minusDays(7).atStartOfDay(ZoneOffset.UTC).toLocalDateTime()

            val errors = mutableListOf<String>()
            val events = eventProviders()
                    .flatMap {
                        try {
                            it.events(since)
                        } catch (e: Exception) {
                            errors.add(e.message ?: "Unknown error")
                            emptyList<Event>()
                        }
                    }
                    .sortedByDescending { it.timestamp }
                    .map { it.toEventDetail() }

            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(EventList(events, errors)))
        }
        catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }
    }

    private fun eventProviders(): List<EventsProvider> {
        return config.mapNotNull {
            when (it) {
                is InternalEventsProviderConfig -> factory.create(it)
                is ElasticEventsProviderConfig -> factory.create(it)
                else -> null
            }
        }
    }
    
}

interface EventsProviderFactory {
    fun create(config: InternalEventsProviderConfig): InternalEventsProvider
    fun create(config: ElasticEventsProviderConfig): ElasticEventsProvider
}


fun Event.toEventDetail() = EventDetail(
        timestamp = this.timestamp.toDateUtc,
        type = this.type,
        message = this.message,
        envId =  this.envId,
        instanceId = this.instanceId,
        version = this.version
)
