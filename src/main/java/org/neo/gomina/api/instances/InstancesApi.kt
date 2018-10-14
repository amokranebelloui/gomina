package org.neo.gomina.api.instances

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.runtime.ExtInstance
import org.neo.gomina.model.runtime.Topology
import org.neo.gomina.api.toDateUtc
import org.neo.gomina.integration.scm.ScmService
import org.neo.gomina.integration.ssh.SshService
import org.neo.gomina.model.host.InstanceSshDetails
import org.neo.gomina.model.host.resolveHostname
import org.neo.gomina.model.inventory.Instance
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.inventory.Service
import org.neo.gomina.model.inventory.ServiceMode
import org.neo.gomina.model.monitoring.Monitoring
import org.neo.gomina.model.monitoring.RuntimeInfo
import org.neo.gomina.model.monitoring.ServerStatus
import org.neo.gomina.model.project.Projects
import org.neo.gomina.model.scm.ScmDetails
import java.util.*
import javax.inject.Inject

data class ServiceDetail (
        val svc: String,
        val type: String? = null,
        val mode: ServiceMode? = ServiceMode.ONE_ONLY,
        val activeCount: Int = 1,
        val project: String? = null
)

data class VersionDetail(val version: String = "", val revision: String = "")
data class VersionsDetail(val running: VersionDetail?, val deployed: VersionDetail?, val released: VersionDetail?, val latest: VersionDetail?)

data class SidecarDetail(val status: String = "", val version: String = "", val revision: String = "")

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

        var project: String? = null,
        var deployHost: String? = null,
        @Deprecated("") var deployFolder: String? = null,
        @Deprecated("") var confCommited: Boolean? = null,
        @Deprecated("") var confUpToDate: Boolean? = null,
        @Deprecated("") var confRevision: String? = null,

        // Versions
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

        var properties: HashMap<String, Any?> = hashMapOf()
)

class InstancesApi {

    companion object {
        private val logger = LogManager.getLogger(InstancesApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject private lateinit var inventory: Inventory
    @Inject private lateinit var projects: Projects

    @Inject lateinit private var scmService: ScmService
    @Inject lateinit private var sshService: SshService

    @Inject lateinit private var monitoring: Monitoring
    @Inject lateinit private var topology: Topology

    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.get("/").handler(this::instances)
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

            val instances = inventory.getEnvironment(envId)?.let {
                topology.buildExtInstances(it)
                        .groupBy { it.service }
                        .map { (service, extInstances) ->
                            mapOf(
                                    "service" to service.toServiceDetail(),
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
                projects.getProjects()
                        .mapNotNull { it.scm }
                        .forEach { scmService.reloadScmDetails(it) }
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

private fun buildInstanceDetail(envId: String, ext: ExtInstance): InstanceDetail {

    val id = envId + "-" + ext.id
    val instance = InstanceDetail(id = id, env = envId, type = ext.service.type, service = ext.service.svc, name = ext.id, unexpected = ext.notExpected)
    ext.instance?.let {
        instance.applyInventory(ext.service, it)
        if (ext.indicators == null) {
            instance.status = ServerStatus.OFFLINE
        }
    }
    ext.sshDetails?.let { instance.applySsh(it) }
    ext.indicators?.let {
        instance.applyMonitoring(it)
        instance.applyCluster(it)
        instance.applyRedis(it)
        instance.unexpectedHost = StringUtils.isNotBlank(instance.deployHost) && instance.deployHost != instance.host
    }
    ext.scmDetail?.let { instance.applyScm(it) }
    instance.versions = versions(ext.scmDetail, ext.sshDetails, ext.indicators)
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

private fun versions(scmDetails: ScmDetails?, instanceSshDetails: InstanceSshDetails?, indicators: RuntimeInfo?) =
        VersionsDetail(
                running = indicators?.version?.let { VersionDetail(it.version ?: "", it.revision ?: "") },
                deployed = instanceSshDetails?.let { VersionDetail(it.deployedVersion ?: "", it.deployedRevision ?: "") },
                released = scmDetails?.let { VersionDetail(it.released ?: "", it.releasedRevision ?: "") },
                latest = scmDetails?.let { VersionDetail(it.latest ?: "", it.latestRevision ?: "") }
        )

private fun InstanceDetail.applyScm(scmDetails: ScmDetails) {
    this.latestVersion = scmDetails.latest
    this.latestRevision = scmDetails.latestRevision
    this.releasedVersion = scmDetails.released
    this.releasedRevision = scmDetails.releasedRevision
}

private fun InstanceDetail.applySsh(instanceSshDetails: InstanceSshDetails) {
    this.deployVersion = instanceSshDetails.deployedVersion
    this.deployRevision = instanceSshDetails.deployedRevision
    this.confCommited = instanceSshDetails.confCommitted
    this.confUpToDate = instanceSshDetails.confUpToDate
    this.confRevision = instanceSshDetails.confRevision
}

// FIXME Redundant
private fun InstanceDetail.applyMonitoring(indicators: RuntimeInfo) {
    this.pid = indicators.process.pid
    this.host = resolveHostname(indicators.process.host)
    this.version = indicators.version.version
    this.revision = indicators.version.revision
    this.status = indicators.process.status
    this.startTime = indicators.process.startTime?.toDateUtc
    this.startDuration = indicators.process.startDuration

    this.sidecar = SidecarDetail(status = indicators.sidecarStatus ?: "", version = indicators.sidecarVersion ?: "")

    this.properties.put("jvm.jmx.port", indicators.jvm.jmx?.toString())
    this.properties.put("xxx.bux.version", indicators.dependencies.busVersion)
    this.properties.put("xxx.core.version", indicators.dependencies.coreVersion)
    this.properties.put("quickfix.persistence", indicators.fix.quickfixPersistence)

}

private fun InstanceDetail.applyCluster(indicators: RuntimeInfo) {
    this.cluster = indicators.cluster.cluster
    this.participating = indicators.cluster.participating
    this.leader = indicators.cluster.leader
}

private fun InstanceDetail.applyRedis(indicators: RuntimeInfo) {
    // FIXME Move to dynamic fields for instance details
    this.properties.put("redis.host", indicators.redis?.redisHost)
    this.properties.put("redis.port", indicators.redis?.redisPort)
    this.properties.put("redis.master", indicators.redis?.redisMaster)
    this.properties.put("redis.status", indicators.redis?.redisStatus)
    this.properties.put("redis.role", indicators.redis?.redisRole)
    this.properties.put("redis.rw", indicators.redis?.redisRW)
    this.properties.put("redis.persistence.mode", indicators.redis?.redisMode)
    this.properties.put("redis.offset", indicators.redis?.redisOffset)
    this.properties.put("redis.slave.count", indicators.redis?.redisSlaveCount)
    this.properties.put("redis.client.count", indicators.redis?.redisClientCount)
    if (indicators.redis?.redisRole == "SLAVE") {
        this.properties.put("redis.master.host", indicators.redis.redisMasterHost)
        this.properties.put("redis.master.port", indicators.redis.redisMasterPort)
        this.properties.put("redis.master.link", mapOf(
                "status" to indicators.redis.redisMasterLink,
                "downSince" to indicators.redis.redisMasterLinkDownSince)
        )
        this.properties.put("redis.master.offset.diff", indicators.redis?.redisOffsetDiff)
    }

}

fun Service.toServiceDetail(): ServiceDetail {
    return ServiceDetail(
            svc = this.svc,
            type = this.type,
            mode = this.mode,
            activeCount = this.activeCount,
            project = this.project)
}

//fun Version.toVersionDetail() = VersionDetail(version = this.version, revision = this.revision.toString())
