package org.neo.gomina.api.system

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.system.System
import org.neo.gomina.model.system.Systems
import javax.inject.Inject

data class SystemDetail(
        var id: String,
        var components: Int,
        var loc: Double? = null,
        var locBasis: Int,
        var coverage: Double? = null,
        var coverageBasis: Int
)

class SystemsApi {

    companion object {
        private val logger = LogManager.getLogger(SystemsApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject private lateinit var systems: Systems
    @Inject private lateinit var componentRepo: ComponentRepo

    private val mapper = ObjectMapper().registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.get("/refs").handler(this::systemsRefs)
        router.get("/").handler(this::systems)
    }


    private fun systemsRefs(ctx: RoutingContext) {
        try {
            ctx.response().putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(systems.getSystems()))
        } catch (e: Exception) {
            logger.error("Cannot get Systems", e)
            ctx.fail(500)
        }
    }

    private fun systems(ctx: RoutingContext) {
        try {
            val systems = componentRepo.getAll()
                    .flatMap { comp -> System.extend(comp.systems).map { it to comp } }
                    .groupBy({ (s, _) -> s }) { (_, components) -> components }
                    .map { (s, components) ->
                        val loc = components.fold(0 to 0.0) { (count, sum), c ->
                            c.loc?.let { count + 1 to sum + it } ?: count to sum
                        }
                        val coverage = components.fold(0 to 0.0) { (count, sum), c ->
                            c.coverage?.let { count + 1 to sum + it } ?: count to sum
                        }
                        SystemDetail(
                                id = s,
                                components = components.size,
                                loc = loc.second,
                                locBasis = loc.first,
                                coverage = coverage.second,
                                coverageBasis = coverage.first
                        )
                    }
            ctx.response().putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(systems))
        } catch (e: Exception) {
            logger.error("Cannot get Systems", e)
            ctx.fail(500)
        }
    }

}