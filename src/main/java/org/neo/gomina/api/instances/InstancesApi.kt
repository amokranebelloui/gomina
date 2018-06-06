package org.neo.gomina.api.instances

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.monitoring.*
import org.neo.gomina.integration.scm.ScmDetails
import org.neo.gomina.integration.scm.ScmService
import org.neo.gomina.integration.ssh.InstanceSshDetails
import org.neo.gomina.integration.ssh.SshService
import org.neo.gomina.model.host.resolveHostname
import org.neo.gomina.model.inventory.Environment
import org.neo.gomina.model.inventory.Instance
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.inventory.Service
import org.neo.gomina.model.project.Project
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

    @Inject lateinit private var scmService: ScmService 
    @Inject lateinit private var sshService: SshService

    @Inject lateinit private var monitoring: Monitoring

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

    @Inject
    fun prepare() {
        fun mapStatus(status: String?) = if ("SHUTDOWN" == status) "DOWN" else status ?: "DOWN"
        monitoring.enrich = { indicators ->
            indicators.put("TIMESTAMP", Date().toString())
            indicators["status"]?.let { status -> indicators.put("STATUS", mapStatus(status)) }
        }
        monitoring.include = { it["STATUS"] != null && it["VERSION"] != null }
        monitoring.checkFields(setOf("PARTICIPATING", "LEADER", "STATUS"))
        monitoring.onDelay {
            mapOf("STATUS" to "NOINFO")
        }
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
        ext.instance?.let {
            instance.applyInventory(ext.service, ext.instance)
            if (ext.indicators == null) {
                instance.status = "NOINFO"
            }
            sshService.getDetails(ext.instance)?.let { instance.applySsh(it) }
        }
        ext.indicators?.let {
            instance.applyMonitoring(ext.indicators)
            instance.applyCluster(ext.indicators)
            instance.applyRedis(ext.indicators)
        }
        ext.project?.let {
            scmService.getScmDetails(it, fromCache = true)?.let { instance.applyScm(it) }
        }
        return instance
    }

    private data class ExtInstance(val id:String, val service:Service, val project:Project?, val instance: Instance?, val indicators: Indicators?)

    private fun buildExtInstances(env: Environment): List<ExtInstance> {
        val services = env ?. services ?. associateBy { it.svc }
        val inventory = env.services
                .flatMap { svc -> svc.instances.map { instance -> svc to instance } }
                .associateBy { (_, instance) -> instance.id }
        val monitoring = monitoring.instancesFor(env.id)
                .associateBy { it.instanceId }
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
                projects.getProjects()
                        .forEach { scmService.getScmDetails(it, fromCache = false) }
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

private fun InstanceDetail.applyInventory(service: Service, envInstance: Instance) {
    this.unexpected = false
    this.type = service.type
    this.service = service.svc
    this.project = service.project
    this.deployHost = envInstance.host
    this.deployFolder = envInstance.folder
}

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

private fun InstanceDetail.applyMonitoring(indicators: Indicators) {
    this.pid = indicators["PID"]
    this.host = resolveHostname(indicators["IP"])
    this.version = indicators["VERSION"]
    this.revision = indicators["REVISION"]
    this.unexpectedHost = StringUtils.isNotBlank(this.deployHost) && this.deployHost != this.host
    this.status = indicators["STATUS"]

    this.jmx = indicators["JMX"].asInt
    this.busVersion = indicators["BUS"]
    this.coreVersion = indicators["CORE"]
    this.quickfixPersistence = indicators["QUICKFIX_MODE"]
}

private fun InstanceDetail.applyCluster(indicators: Indicators) {
    this.cluster = indicators["ELECTION"].asBoolean ?: false
    this.participating = indicators["PARTICIPATING"].asBoolean ?: false
    this.leader = indicators["LEADER"].asBoolean ?: true // Historically we didn't have this field
}

private fun InstanceDetail.applyRedis(indicators: Indicators) {
    this.redisHost = indicators["REDIS_HOST"]
    this.redisPort = indicators["REDIS_PORT"].asInt
    this.redisMasterHost = indicators["REDIS_MASTER_HOST"]
    this.redisMasterPort = indicators["REDIS_MASTER_PORT"].asInt
    this.redisMasterLink = "up" == indicators["REDIS_MASTER_LINK"]
    this.redisMasterLinkDownSince = indicators["REDIS_MASTER_LINK_DOWN_SINCE"]
    this.redisOffset = indicators["REDIS_OFFSET"].asLong
    this.redisOffsetDiff = indicators["REDIS_OFFSET_DIFF"].asLong
    this.redisMaster = indicators["REDIS_MASTER"].asBoolean
    this.redisRole = indicators["REDIS_ROLE"]
    this.redisRW = if ("yes".equals(indicators["REDIS_READONLY"], ignoreCase = true)) "ro" else "rw"
    this.redisMode = if ("1" == indicators["REDIS_AOF"]) "AOF" else "RDB"
    this.redisStatus = indicators["REDIS_STATE"]
    this.redisSlaveCount = indicators["REDIS_SLAVES"].asInt
    this.redisClientCount = indicators["REDIS_CLIENTS"].asInt
}
