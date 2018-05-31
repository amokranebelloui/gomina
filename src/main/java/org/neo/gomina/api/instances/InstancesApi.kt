package org.neo.gomina.api.instances

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.monitoring.Indicators
import org.neo.gomina.model.inventory.Environment
import org.neo.gomina.model.inventory.Instance
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.inventory.Service
import org.neo.gomina.model.project.Project
import org.neo.gomina.model.project.Projects
import org.neo.gomina.plugins.monitoring.MonitoringPlugin
import org.neo.gomina.plugins.monitoring.applyCluster
import org.neo.gomina.plugins.monitoring.applyMonitoring
import org.neo.gomina.plugins.monitoring.applyRedis
import org.neo.gomina.plugins.scm.ScmPlugin
import org.neo.gomina.plugins.scm.applyScm
import org.neo.gomina.plugins.ssh.SshPlugin
import javax.inject.Inject

class InstancesApi {

    companion object {
        private val logger = LogManager.getLogger(InstancesApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject private lateinit var inventory: Inventory
    @Inject private lateinit var projects: Projects

    @Inject lateinit private var monitoringPlugin: MonitoringPlugin
    @Inject lateinit private var scmPlugin: ScmPlugin
    @Inject lateinit private var sshPlugin: SshPlugin

    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.get("/").handler(this::instances)
        router.get("/:envId").handler(this::forEnv)
        router.get("/:envId/services").handler(this::servicesForEnv)

        router.post("/:envId/reload-inventory").handler(this::reloadInv)
        router.post("/:envId/reload-scm").handler(this::reloadScm)
        router.post("/:envId/reload-ssh").handler(this::reloadSsh)
    }

    fun instances(ctx: RoutingContext) {
        try {
            val instances = inventory.getEnvironments().flatMap { env ->
                buildExtInstances(env).map { build(env.id, it) }
            }
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(instances))
        } catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }
    }

    fun forEnv(ctx: RoutingContext) {
        try {
            val envId = ctx.request().getParam("envId")
            val instances = inventory.getEnvironment(envId) ?. let {
                buildExtInstances(it).map { build(envId, it) }
            }
            ?: emptyList()
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(instances))
        } catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }
    }

    fun servicesForEnv(ctx: RoutingContext) {
        try {
            val envId = ctx.request().getParam("envId")

            val instances = inventory.getEnvironment(envId)?.let {
                buildExtInstances(it)
                        .groupBy { it.service }
                        .map { (service, extInstances) ->
                            mapOf(
                                    "service" to service.toServiceDetail(),
                                    "instances" to extInstances.map { build(envId, it) }
                            )
                        }
            }
            ?: emptyList()
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(instances))
        } catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }
    }

    private fun build(envId: String, ext: ExtInstance): InstanceDetail {
        val expected = ext.instance != null
        val id = envId + "-" + ext.id
        val instance = InstanceDetail(id = id, env = envId, type = ext.service.type, service = ext.service.svc, name = ext.id, unexpected = !expected)
        ext.instance?.let { instance.applyInventory(ext.service, ext.instance) }
        ext.indicators?.let {
            instance.applyMonitoring(ext.indicators)
            instance.applyCluster(ext.indicators)
            instance.applyRedis(ext.indicators)
        }

        ext.project?.let { instance.applyScm(scmPlugin.getSvnDetails(it.svnRepo, it.svnUrl)) }
        ext.instance?.let { sshPlugin.enrich(ext.instance, instance) }
        return instance
    }

    private fun InstanceDetail.applyInventory(service: Service, envInstance: Instance) {
        this.unexpected = false
        this.type = service.type
        this.service = service.svc
        this.project = service.project
        this.deployHost = envInstance.host
        this.deployFolder = envInstance.folder
    }


    private data class ExtInstance(val id:String, val service:Service, val project:Project?, val instance: Instance?, val indicators: Indicators?)

    private fun buildExtInstances(env: Environment): List<ExtInstance> {
        val services = env ?. services ?. associateBy { it.svc }
        val inventory = env.services.flatMap { svc -> svc.instances.map { Pair(svc, it) } }.associateBy { it.second.id }
        val monitoring = monitoringPlugin.monitoring.instancesFor(env.id).associateBy { it.instanceId }
        return merge(inventory, monitoring)
                .map { (id, instance, indicators) ->
                    val svc = instance?.first?.svc ?: indicators?.get("SERVICE") ?: "x"
                    val service = services[svc] ?: Service(svc = svc, type = indicators?.get("TYPE"))
                    val project = service.project?.let { projects.getProject(it) }
                    ExtInstance(id, service, project, instance?.second, indicators)
                }
    }

    private fun reloadScm(ctx: RoutingContext) {
        try {
            vertx.executeBlocking({future: Future<Void> ->
                val envId = ctx.request().getParam("envId")
                logger.info("Reloading SCM data $envId ...")
                scmPlugin.reloadInstances(envId)
                future.complete()
            }, false)
            {res: AsyncResult<Void> ->
                ctx.response().putHeader("content-type", "text/javascript").end("reload SCM done!")
            }
        }
        catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }
    }

    private fun reloadSsh(ctx: RoutingContext) {
        try {
            vertx.executeBlocking({future: Future<Void> ->
                val envId = ctx.request().getParam("envId")
                logger.info("Reloading SSH data $envId ...")
                sshPlugin.reloadInstances(envId)
                future.complete()
            }, false)
            {res: AsyncResult<Void> ->
                ctx.response().putHeader("content-type", "text/javascript").end("reload SSH done!")
            }
        }
        catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }
    }

    private fun reloadInv(ctx: RoutingContext) {
        try {
            vertx.executeBlocking({future: Future<Void> ->
                val envId = ctx.request().getParam("envId")
                logger.info("Reloading inventory data ...")
                //inventoryPlugin.reload(envId)
                future.complete()
            }, false)
            {res: AsyncResult<Void> ->
                ctx.response().putHeader("content-type", "text/javascript").end("reload inventory done!")
            }
        }
        catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }
    }

}

fun <T, R> merge(map1:Map<String, T>, map2:Map<String, R>): Collection<Triple<String, T?, R?>> {
    val result = mutableMapOf<String, Triple<String, T?, R?>>()
    map1.forEach { (id, t) -> result.put(id, Triple(id, t, map2[id])) }
    map2.forEach { (id, r) -> if (!map1.contains(id)) result.put(id, Triple(id, null, r)) }
    return result.values
}

fun main(args: Array<String>) {
    val m1 = mapOf("1" to 1, "2" to 2)
    val m2 = mapOf("2" to 20.2, "3" to 30.3)
    println(merge(m1, m2))
}


fun Service.toServiceDetail(): ServiceDetail {
    return ServiceDetail(svc = this.svc, type = this.type, project = this.project)
}