package org.neo.gomina.api.envs

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.inventory.Inventory
import javax.inject.Inject

/**
 * type: PROD, TEST
 */
data class Env(
    val env: String,
    val type:String,
    val app:String
)

class EnvBuilder {
    @Inject private lateinit var inventory: Inventory
    fun getEnvs(): Collection<Env> {
        return inventory.getEnvironments().map {
            Env(it.id, it.type, "myproject") // FIXME project
        }
    }
}

class EnvsApi {

    companion object {
        private val logger = LogManager.getLogger(EnvsApi::class.java)
    }

    val router: Router

    @Inject private lateinit var envBuilder: EnvBuilder
    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.router = Router.router(vertx)

        router.get("/").handler(this::data)
    }

    fun data(ctx: RoutingContext) {
        try {
            ctx.response().putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(envBuilder.getEnvs()))
        }
        catch (e: Exception) {
            logger.error("Cannot get projects", e)
            ctx.fail(500)
        }
    }

}