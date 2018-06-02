package org.neo.gomina.api.events

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.name.Named
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.elasticsearch.ElasticEvents
import org.neo.gomina.integration.eventrepo.EventRepo
import org.neo.gomina.model.event.Event
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

    @Inject lateinit var eventRepo: EventRepo
    @Inject @Named("releases") lateinit var releaseEvents: ElasticEvents

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
            val since = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toLocalDateTime()
            val all = eventRepo.getEvents(since) + releaseEvents.getEvents(since)
            val events = all
                    .sortedByDescending { it.timestamp }
                    .map { it.toEventDetail() }
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(events))
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
