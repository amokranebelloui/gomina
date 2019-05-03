package org.neo.gomina.api.instances

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.api.common.toDateUtc
import org.neo.gomina.api.component.ComponentRef
import org.neo.gomina.integration.scm.ScmService
import org.neo.gomina.integration.ssh.SshService
import org.neo.gomina.model.component.Component
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.inventory.Service
import org.neo.gomina.model.inventory.ServiceMode
import org.neo.gomina.model.monitoring.Monitoring
import org.neo.gomina.model.monitoring.ServerStatus
import org.neo.gomina.model.runtime.ExtInstance
import org.neo.gomina.model.runtime.Topology
import org.neo.gomina.model.version.Version
import java.util.*
import javax.inject.Inject

data class ServiceDetail (
        val svc: String,
        val type: String? = null,
        val mode: ServiceMode? = ServiceMode.ONE_ONLY,
        val activeCount: Int? = 1,
        @Deprecated("") val componentId: String? = null,
        val component: ComponentRef? = null,
        val systems: List<String>
)

data class VersionDetail(val version: String = "", val revision: String?)
data class VersionsDetail(val running: VersionDetail?, val deployed: VersionDetail?, val released: VersionDetail?, val latest: VersionDetail?)

data class SidecarDetail(val status: String = "", val version: String = "", val revision: String = "")

data class InstanceData(val host: String?, val folder: String?)
data class ServiceData (val svc: String, val type: String? = null,
                        val mode: ServiceMode?, val activeCount: Int?,
                        val componentId: String? = null)

class InstanceDetail(

        var env: String? = null,
        var id: String? = null, // Unique by env
        var name: String? = null,// X Replication
        var service: String? = null, // Z Partitioning
        var type: String? = null, // Y Functional

        var unexpected: Boolean = false,
        var unexpectedHost: Boolean = false,

        var cluster: Boolean = false,
        var participating: Boolean = false,
        var leader: Boolean = false,

        var pid: String? = null,
        var host: String? = null,
        var status: String? = null,
        var startTime: Date? = null,
        var startDuration: Long? = null,

        var componentId: String? = null,
        var deployHost: String? = null,
        var deployFolder: String? = null,
        var confCommited: Boolean? = null,
        var confUpToDate: Boolean? = null,
        var confRevision: String? = null,

        // FIXME Deprecated Versions
        /**/
        @Deprecated("") var version: String? = null,
        @Deprecated("") var revision: String? = null,
        @Deprecated("") var deployVersion: String? = null,
        @Deprecated("") var deployRevision: String? = null,
        @Deprecated("") var releasedVersion: String? = null,
        @Deprecated("") var releasedRevision: String? = null,
        @Deprecated("") var latestVersion: String? = null,
        @Deprecated("") var latestRevision: String? = null,
        /**/
        var versions: VersionsDetail? = null,

        var sidecar: SidecarDetail? = null,

        var properties: Map<String, Any?> = mapOf()
)

class InstancesApi {

    companion object {
        private val logger = LogManager.getLogger(InstancesApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject private lateinit var inventory: Inventory
    @Inject private lateinit var componentRepo: ComponentRepo

    @Inject lateinit private var scmService: ScmService
    @Inject lateinit private var sshService: SshService

    @Inject lateinit private var monitoring: Monitoring
    @Inject lateinit private var topology: Topology

    private val mapper = ObjectMapper()
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.get("/").handler(this::instances)

        router.post("/:envId/service/add").handler(this::addService)
        router.put("/:envId/service/:svcId/update").handler(this::updateService)
        router.put("/:envId/service/:svcId/reorder").handler(this::reorderService)
        router.delete("/:envId/service/:svcId/delete").handler(this::deleteService)

        router.post("/:envId/service/:svcId/instance/add").handler(this::addInstance)
        router.delete("/:envId/service/:svcId/instance/:instanceId/delete").handler(this::deleteInstance)

        //router.get("/:envId").handler(this::forEnv)
        router.get("/:envId/services").handler(this::servicesForEnv)

        router.post("/:envId/reload-inventory").handler(this::reloadInv)
        router.post("/:envId/reload-scm").handler(this::reloadScm)
        router.post("/:envId/reload-ssh").handler(this::reloadSsh)
    }

    @Inject
    fun prepare() {
        monitoring.fieldsChanged { a, b ->
            a.cluster.participating != b.cluster.participating ||
            a.cluster.leader != b.cluster.leader ||
            a.process.status != b.process.status
        }
    }

    private fun addService(ctx: RoutingContext) {
        val svc = ctx.request().getParam("svcId")
        val env = ctx.request().getParam("envId")
        try {
            val data = mapper.readValue<ServiceData>(ctx.body.toString())
            logger.info("Adding svc $svc/$env $data")
            inventory.addService(env, svc, data.type, data.mode, data.activeCount, data.componentId)
            ctx.response().putHeader("content-type", "text/javascript").end(mapper.writeValueAsString(svc))
        }
        catch (e: Exception) {
            logger.error("Cannot add Service", e)
            ctx.fail(500)
        }
    }

    private fun updateService(ctx: RoutingContext) {
        val svc = ctx.request().getParam("svcId")
        val env = ctx.request().getParam("envId")
        try {
            val data = mapper.readValue<ServiceData>(ctx.body.toString())
            logger.info("Updating svc $svc/$env $data")

            if (svc != data.svc) {
                inventory.renameService(env, svc, data.svc)
            }
            inventory.updateService(env, svc, data.type, data.mode, data.activeCount, data.componentId)

            ctx.response().putHeader("content-type", "text/javascript").end(mapper.writeValueAsString(svc))
        }
        catch (e: Exception) {
            logger.error("Cannot Update Service", e)
            ctx.fail(500)
        }
    }

    private fun reorderService(ctx: RoutingContext) {
        val env = ctx.request().getParam("envId")
        val svc = ctx.request().getParam("svcId")
        val target = ctx.request().getParam("target")
        try {
            logger.info("Reordering svc $svc -> $target")
            inventory.reorderService(env, svc, target)
            ctx.response().putHeader("content-type", "text/javascript").end(mapper.writeValueAsString(svc))
        }
        catch (e: Exception) {
            logger.error("Cannot Update Service", e)
            ctx.fail(500)
        }
    }

    private fun deleteService(ctx: RoutingContext) {
        val svc = ctx.request().getParam("svcId")
        val env = ctx.request().getParam("envId")
        try {
            logger.info("Deleting svc $svc/$env")
            inventory.deleteService(env, svc)
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot delete Service", e)
            ctx.fail(500)
        }
    }

    private fun addInstance(ctx: RoutingContext) {
        val instanceId = ctx.request().getParam("instanceId")
        val svc = ctx.request().getParam("svcId")
        val env = ctx.request().getParam("envId")
        try {
            val instanceData = mapper.readValue<InstanceData>(ctx.body.toString())
            logger.info("Adding Instance $instanceId/$svc/$env $instanceData")
            inventory.addInstance(env, svc, instanceId, instanceData.host, instanceData.folder)
            ctx.response().putHeader("content-type", "text/javascript").end(instanceId)
        }
        catch (e: Exception) {
            logger.error("Cannot add Instance", e)
            ctx.fail(500)
        }
    }

    private fun deleteInstance(ctx: RoutingContext) {
        val instanceId = ctx.request().getParam("instanceId")
        val svc = ctx.request().getParam("svcId")
        val env = ctx.request().getParam("envId")
        try {
            logger.info("Deleting Instance $instanceId/$svc/$env")
            inventory.deleteInstance(env, svc, instanceId)
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot delete Instance", e)
            ctx.fail(500)
        }
    }

    private fun instances(ctx: RoutingContext) {
        try {
            val instances = inventory.getEnvironments()
                    .flatMap { env -> topology.buildExtInstances(env).map { buildInstanceDetail(env.id, it) } }
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(instances))
        } catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }
    }

    /*
    private fun forEnv(ctx: RoutingContext) {
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
    */

    private fun servicesForEnv(ctx: RoutingContext) {
        try {
            val envId = ctx.request().getParam("envId")

            val components = componentRepo.getAll().associateBy { it.id }

            val res = mutableMapOf<Service, MutableList<ExtInstance>>()

            val instances = inventory.getEnvironment(envId)?.let {
                it.services.forEach { service ->
                    res.getOrPut(service) { mutableListOf() }
                }
                topology.buildExtInstances(it)
                        .forEach {
                            res.getOrPut(it.service) { mutableListOf() }.add(it)
                        }
                        /*
                        .groupBy { it.service }
                        */
                res.map { (service, extInstances) ->
                    mapOf(
                            "service" to service.toServiceDetail(components[service.componentId]),
                            "instances" to extInstances.map { buildInstanceDetail(envId, it) }
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

    private fun reloadScm(ctx: RoutingContext) {
        try {
            vertx.executeBlocking({future: Future<Void> ->
                val envId = ctx.request().getParam("envId")
                logger.info("Reloading SCM data $envId ...")
                componentRepo.getAll().forEach { component ->
                    component.scm?.let { scmService.reloadScmDetails(component, it) }
                }
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
                inventory.getEnvironment(envId)?.let { sshService.processEnv(it) }
                future.complete()
            }, false)
            {res: AsyncResult<Void> ->
                when {
                    res.succeeded() -> ctx.response().putHeader("content-type", "text/javascript").end("reload SSH done!")
                    res.failed() -> ctx.fail(500)
                }

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

private fun buildInstanceDetail(envId: String, ext: ExtInstance): InstanceDetail {
    val instance = InstanceDetail(id = ext.completeId,
            env = envId, type = ext.service.type, service = ext.service.svc, name = ext.instanceId,
            unexpected = ext.notExpected)

    instance.type = ext.service.type
    instance.service = ext.service.svc
    instance.componentId = ext.service.componentId

    instance.versions = VersionsDetail(
            running = ext.indicators?.version?.let { VersionDetail(it.version, it.revision) },
            deployed = ext.instance?.let { it.deployedVersion?.toVersionDetail() },
            released = ext.component?.let { it.released?.toVersionDetail() },
            latest = ext.component?.let { it.latest?.toVersionDetail() }
    )

    ext.instance?.let {
        instance.unexpected = false
        instance.deployHost = it.host
        instance.deployFolder = it.folder
        if (ext.indicators == null) {
            instance.status = ServerStatus.OFFLINE
        }
        instance.deployVersion = it.deployedVersion?.version
        instance.deployRevision = it.deployedVersion?.revision
        instance.confCommited = it.confCommitted
        instance.confUpToDate = it.confUpToDate
        instance.confRevision = it.confRevision
    }
    ext.indicators?.let {
        instance.pid = it.process.pid
        instance.host = it.process.host
        instance.version = it.version?.version
        instance.revision = it.version?.revision
        instance.status = it.process.status
        instance.startTime = it.process.startTime?.toDateUtc
        instance.startDuration = it.process.startDuration
        instance.sidecar = SidecarDetail(status = it.sidecarStatus ?: "", version = it.sidecarVersion ?: "")
        instance.properties = it.properties

        instance.cluster = it.cluster.cluster
        instance.participating = it.cluster.participating
        instance.leader = it.cluster.leader

        instance.unexpectedHost = StringUtils.isNotBlank(instance.deployHost) && instance.deployHost != instance.host
    }
    ext.component?.let {
        instance.latestVersion = it.latest?.version
        instance.latestRevision = it.latest?.revision
        instance.releasedVersion = it.released?.version
        instance.releasedRevision = it.released?.revision
    }

    return instance
}

fun Service.toServiceDetail(component: Component?): ServiceDetail {
    return ServiceDetail(
            svc = this.svc,
            type = this.type,
            mode = this.mode,
            activeCount = this.activeCount,
            componentId = this.componentId,
            component = component?.let { ComponentRef(component.id, component.label) },
            systems = component?.systems ?: emptyList())
}

fun Version.toVersionDetail() = VersionDetail(version = this.version, revision = this.revision)
