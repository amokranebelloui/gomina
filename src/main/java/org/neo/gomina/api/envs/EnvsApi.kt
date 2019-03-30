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

class EnvsApi {

    companion object {
        private val logger = LogManager.getLogger(EnvsApi::class.java)
    }

    val router: Router

    @Inject private lateinit var inventory: Inventory
    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.router = Router.router(vertx)

        router.get("/").handler(this::data)
        router.post("/add").handler(this::addEnv)
        router.delete("/:envId/delete").handler(this::deleteEnv)
    }

    private fun data(ctx: RoutingContext) {
        try {
            val envs = inventory.getEnvironments().map { Env(it.id, it.type, "My System") } // FIXME system
            ctx.response().putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(envs))
        }
        catch (e: Exception) {
            logger.error("Cannot get envs", e)
            ctx.fail(500)
        }
    }

    private fun addEnv(ctx: RoutingContext) {
        val envId = ctx.request().getParam("envId")
        try {
            val body = ctx.body.toString()
            logger.info("Adding env $envId $body [TODO]")
            Thread.sleep(1000)
            // FIXME Implement
            val env = Env(envId, "DUMMY", "My System")
            ctx.response().putHeader("content-type", "text/javascript").end(mapper.writeValueAsString(env))
        }
        catch (e: Exception) {
            logger.error("Cannot add Env", e)
            ctx.fail(500)
        }
    }

    private fun deleteEnv(ctx: RoutingContext) {
        val envId = ctx.request().getParam("envId")
        try {
            val body = ctx.body.toString()
            logger.info("Deleting env $envId $body [TODO]")
            Thread.sleep(1000)
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot delete Env", e)
            ctx.fail(500)
        }
    }

}