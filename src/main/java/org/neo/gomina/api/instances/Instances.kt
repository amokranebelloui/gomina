package org.neo.gomina.api.instances

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.instances.Instance
import org.neo.gomina.core.instances.Instances
import org.neo.gomina.model.inventory.Environment
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.project.Projects
import org.neo.gomina.plugins.inventory.InventoryPlugin
import org.neo.gomina.plugins.monitoring.Monitoring
import org.neo.gomina.plugins.scm.ScmPlugin
import org.neo.gomina.plugins.ssh.DumbSshConnector
import javax.inject.Inject

class InstancesApi {

    companion object {
        private val logger = LogManager.getLogger(InstancesApi::class.java)
    }

    val router: Router

    @Inject private lateinit var inventory: Inventory
    @Inject private lateinit var projects: Projects

    //@Inject private lateinit var plugins:List<InstancesExt>

    @Inject private lateinit var inventoryPlugin: InventoryPlugin
    @Inject private lateinit var monitoring: Monitoring
    @Inject private lateinit var sshConnector: DumbSshConnector

    @Inject private lateinit var scmPlugin: ScmPlugin

    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.router = Router.router(vertx)

        router.get("/").handler(this::instances)
        router.get("/:envId").handler(this::forEnv)
        router.post("/reload").handler(this::reload)
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
        inventoryPlugin.onGetInstances(env.id, instances)
        scmPlugin.onGetInstances(env.id, instances)
        sshConnector.onGetInstances(env.id, instances)
        monitoring.onGetInstances(env.id, instances)
    }

    fun reload(ctx: RoutingContext) {
        try {
            logger.info("Reloading ...")
            // FIXME Reload

            for (project in projects.getProjects()) {
                if (StringUtils.isNotBlank(project.svnUrl)) {
                    scmPlugin.refresh(project.svnRepo, project.svnUrl)
                }
            }

            ctx.response().putHeader("content-type", "text/javascript").end()
        } catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }

    }
}