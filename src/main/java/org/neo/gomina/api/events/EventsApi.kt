package org.neo.gomina.api.events

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.event.Event
import org.neo.gomina.plugins.events.EventsPlugin
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*
import javax.inject.Inject

class EventsApi {

    companion object {
        private val logger = LogManager.getLogger(EventsApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject lateinit var eventsPlugin: EventsPlugin

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
            val since = LocalDate.now().minusDays(1).atStartOfDay(ZoneOffset.UTC).toLocalDateTime()

            val errors = mutableListOf<String>()
            val events = eventsPlugin.eventProviders()
                    .flatMap {
                        try {
                            it.getEvents(since)
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

}

fun Event.toEventDetail() = EventDetail(
        timestamp = Date.from(this.timestamp.atZone(ZoneOffset.UTC).toInstant()),
        type = this.type,
        message = this.message,
        envId =  this.envId,
        instanceId = this.instanceId,
        version = this.version
)
