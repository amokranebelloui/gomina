package org.neo.gomina.api.instances

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.name.Named
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.instances.Instance
import org.neo.gomina.core.instances.Instances
import org.neo.gomina.core.instances.InstancesExt
import org.neo.gomina.model.inventory.Environment
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.project.Projects
import java.util.*
import javax.inject.Inject

class InstancesApi {

    companion object {
        private val logger = LogManager.getLogger(InstancesApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject private lateinit var inventory: Inventory
    @Inject private lateinit var projects: Projects
    
    @Inject @Named("instances.plugins") lateinit var plugins: ArrayList<InstancesExt>

    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.get("/").handler(this::instances)
        router.get("/:envId").handler(this::forEnv)
    }

    fun instances(ctx: RoutingContext) {
        try {
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(buildInstances()))
        } catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }

    }

    fun forEnv(ctx: RoutingContext) {
        try {
            val envId = ctx.request().getParam("envId")
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(buildInstances(envId)))
        } catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }
    }

    private fun buildInstances(): List<Instance> {
        val instances = Instances()
        inventory.getEnvironments().forEach { buildInstances(it, instances) }
        return instances.list
    }

    private fun buildInstances(envId: String): List<Instance> {
        val instances = Instances()
        val env = inventory.getEnvironment(envId)
        env?.let { buildInstances(env, instances) }
        return instances.list
    }

    private fun buildInstances(env: Environment, instances: Instances) {
        plugins.forEach { it.onGetInstances(env.id, instances) }
    }

}