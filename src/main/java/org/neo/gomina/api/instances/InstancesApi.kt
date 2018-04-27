package org.neo.gomina.api.instances

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.instances.InstanceDetailRepository
import org.neo.gomina.core.instances.ServiceDetail
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.inventory.Service
import org.neo.gomina.model.project.Projects
import javax.inject.Inject

class InstancesApi {

    companion object {
        private val logger = LogManager.getLogger(InstancesApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject private lateinit var inventory: Inventory
    @Inject private lateinit var projects: Projects

    @Inject lateinit var instanceDetailRepository: InstanceDetailRepository

    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.get("/").handler(this::instances)
        router.get("/:envId").handler(this::forEnv)
        router.get("/:envId/services").handler(this::servicesForEnv)
    }

    fun instances(ctx: RoutingContext) {
        try {
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(instanceDetailRepository.getInstances()))
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
                    .end(mapper.writeValueAsString(instanceDetailRepository.getInstances(envId)))
        } catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }
    }

    fun servicesForEnv(ctx: RoutingContext) {
        try {
            val envId = ctx.request().getParam("envId")
            val svcMap = (inventory.getEnvironment(envId)?.services
                    ?.map { it.toServiceDetail() }
                    ?.associateBy { it.svc }
                    ?: emptyMap())
            val serviceMap = svcMap.toMutableMap()

            val instances = instanceDetailRepository.getInstances(envId)
                    .groupBy { it.service ?: "unknown" }
                    .mapKeys { (k, _) -> serviceMap.getOrPut(k) { ServiceDetail(svc = k) } }
                    .map { (service, instances) -> mapOf("service" to service, "instances" to instances) }
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(instances))
        } catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }
    }

}

fun Service.toServiceDetail(): ServiceDetail {
    return ServiceDetail(svc = this.svc, type = this.type, project = this.project)
}