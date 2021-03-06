package org.neo.gomina.api.events

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.api.common.toDateUtc
import org.neo.gomina.dummy.DummyEventsProvider
import org.neo.gomina.dummy.DummyEventsProviderConfig
import org.neo.gomina.integration.elasticsearch.ElasticEventsProvider
import org.neo.gomina.integration.elasticsearch.ElasticEventsProviderConfig
import org.neo.gomina.integration.events.EventsService
import org.neo.gomina.integration.monitoring.MonitoringEventsProvider
import org.neo.gomina.integration.monitoring.MonitoringEventsProviderConfig
import org.neo.gomina.model.event.Event
import org.neo.gomina.model.event.EventCategory
import org.neo.gomina.model.event.Events
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*
import javax.inject.Inject

data class EventList(
        val events: List<EventDetail> = emptyList(),
        val errors: List<String> = emptyList()
)

data class EventDetail(
        val id: String,
        val timestamp: Date,
        val group: EventCategory,
        val type: String?,
        val message: String?,

        val envId: String? = null,
        val instanceId: String? = null,
        val componentId: String? = null,
        val version: String? = null
)

class EventsApi {

    companion object {
        private val logger = LogManager.getLogger(EventsApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject lateinit var eventsService: EventsService
    @Inject lateinit var events: Events

    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.get("/env/:envId").handler(this::eventsForEnv)
        router.get("/component/:componentId").handler(this::eventsForComponent)
        router.post("/reload-events").handler(this::reload)
    }

    private fun eventsForEnv(ctx: RoutingContext) {
        try {
            vertx.executeBlocking({future: Future<Pair<List<EventDetail>, List<String>>> ->
                val envId = ctx.request().getParam("envId")
                logger.info("Events for env $envId")
                val since = LocalDate.now().minusMonths(1).atStartOfDay(ZoneOffset.UTC).toLocalDateTime()

                val errors = mutableListOf<String>()
                val events = events.forEnv(envId)
                        .filter { it.timestamp > since }
                        .sortedByDescending { it.timestamp }
                        .map { it.toEventDetail() }
                future.complete(events to errors)
            }, false)
            {res: AsyncResult<Pair<List<EventDetail>, List<String>>> ->
                ctx.response()
                        .putHeader("content-type", "text/javascript")
                        .end(mapper.writeValueAsString(EventList(res.result().first, res.result().second)))
            }
        }
        catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }
    }

    private fun eventsForComponent(ctx: RoutingContext) {
        try {
            vertx.executeBlocking({future: Future<Pair<List<EventDetail>, List<String>>> ->
                val componentId = ctx.request().getParam("componentId")
                logger.info("Events for component $componentId")
                //val since = LocalDate.now().minusDays(7).atStartOfDay(ZoneOffset.UTC).toLocalDateTime()

                val errors = mutableListOf<String>()
                val events = events.forComponent(componentId)
                        //.filter { it.timestamp > since }
                        .sortedByDescending { it.timestamp }
                        .map { it.toEventDetail() }
                future.complete(events to errors)
            }, false)
            {res: AsyncResult<Pair<List<EventDetail>, List<String>>> ->
                ctx.response()
                        .putHeader("content-type", "text/javascript")
                        .end(mapper.writeValueAsString(EventList(res.result().first, res.result().second)))
            }
        }
        catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }
    }

    private fun reload(ctx: RoutingContext) {
        try {
            eventsService.reload()
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot reload Jenkins", e)
            ctx.fail(500)
        }
    }
}

interface EventsProviderFactory {
    fun create(config: MonitoringEventsProviderConfig): MonitoringEventsProvider
    fun create(config: ElasticEventsProviderConfig): ElasticEventsProvider
    fun create(config: DummyEventsProviderConfig): DummyEventsProvider
}


fun Event.toEventDetail() = EventDetail(
        id = this.id,
        timestamp = this.timestamp.toDateUtc,
        group = this.group,
        type = this.type,
        message = this.message,
        envId =  this.envId,
        instanceId = this.instanceId,
        componentId = this.componentId,
        version = this.version
)
