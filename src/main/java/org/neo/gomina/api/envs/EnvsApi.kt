package org.neo.gomina.api.envs

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
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
    val type: String,
    val description: String?,
    val monitoringUrl: String?,
    val active: Boolean
)

data class EnvData(
        val type: String?,
        val description: String?,
        val monitoringUrl: String?
)

class EnvsApi {

    companion object {
        private val logger = LogManager.getLogger(EnvsApi::class.java)
    }

    val router: Router

    @Inject private lateinit var inventory: Inventory
    private val mapper = ObjectMapper()
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)

    @Inject
    constructor(vertx: Vertx) {
        this.router = Router.router(vertx)

        router.get("/").handler(this::data)
        router.post("/add").handler(this::addEnv)
        router.put("/:envId/update").handler(this::updateEnv)
        router.delete("/:envId/delete").handler(this::deleteEnv)
    }

    private fun data(ctx: RoutingContext) {
        try {
            val envs = inventory.getEnvironments().map { Env(it.id, it.type, it.name, it.monitoringUrl, it.active) }
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
            val data = mapper.readValue<EnvData>(ctx.body.toString())
            logger.info("Adding env $envId $data")
            inventory.addEnvironment(envId, data.type ?: "UNKNOWN", data.description, data.monitoringUrl)
            ctx.response().putHeader("content-type", "text/javascript").end(mapper.writeValueAsString(envId))
        }
        catch (e: Exception) {
            logger.error("Cannot add Env", e)
            ctx.fail(500)
        }
    }

    private fun updateEnv(ctx: RoutingContext) {
        val envId = ctx.request().getParam("envId")
        try {
            val data = mapper.readValue<EnvData>(ctx.body.toString())
            logger.info("Updating env $envId $data")
            inventory.updateEnvironment(envId, data.type ?: "UNKNOWN", data.description, data.monitoringUrl)
            ctx.response().putHeader("content-type", "text/javascript").end(mapper.writeValueAsString(envId))
        }
        catch (e: Exception) {
            logger.error("Cannot add Env", e)
            ctx.fail(500)
        }
    }

    private fun deleteEnv(ctx: RoutingContext) {
        val envId = ctx.request().getParam("envId")
        try {
            logger.info("Deleting env $envId")
            inventory.deleteEnvironment(envId)
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot delete Env", e)
            ctx.fail(500)
        }
    }

}