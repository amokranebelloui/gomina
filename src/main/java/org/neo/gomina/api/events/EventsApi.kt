package org.neo.gomina.api.events

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.events.Event
import org.neo.gomina.plugins.scm.ScmPlugin
import java.util.*
import javax.inject.Inject

class EventsApi {

    companion object {
        private val logger = LogManager.getLogger(EventsApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject lateinit var scmPlugin: ScmPlugin

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
            // Most recent first

            // FIXME Dummy Data
            val events = listOf(
                    Event(Date(2018, 4, 24, 18, 57), "release", "Release order manager", instanceId = "order", version = "2.5.6"),
                    Event(Date(2018, 4, 24, 18, 55), "maintenance", "Purge data"),
                    Event(Date(2018, 4, 22, 17, 12), "outage", "database trading down"),
                    Event(Date(2018, 4, 20, 19, 0), "release", "Release bugfix", instanceId = "basket", version = "1.1.5")
            )
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