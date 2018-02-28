package org.neo.gomina.api.instances

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.neo.gomina.model.inventory.InvInstance
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.inventory.Service
import org.neo.gomina.model.monitoring.Monitoring
import org.neo.gomina.model.project.Projects
import org.neo.gomina.model.scminfo.ScmConnector
import org.neo.gomina.model.scminfo.ScmDetails
import org.neo.gomina.model.scminfo.impl.CachedScmConnector
import org.neo.gomina.model.sshinfo.SshConnector
import org.neo.gomina.model.sshinfo.SshDetails
import java.util.*
import javax.inject.Inject

class Instance (

    var env: String? = null ,
    var id: String? = null, // Unique by env
    var name: String? = null ,// X Replication
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

    var project: String? = null,
    var deployHost: String? = null,
    var deployFolder: String? = null,
    var deployVersion: String? = null,
    var deployRevision: String? = null,
    var confCommited: Boolean? = null,
    var confUpToDate: Boolean? = null,
    var version: String? = null,
    var revision: String? = null,

    var latestVersion: String? = null,
    var latestRevision: String? = null,

    var releasedVersion: String? = null,
    var releasedRevision: String? = null,

    var jmx: Int? = null,
    var busVersion: String? = null,
    var coreVersion: String? = null,
    var quickfixPersistence: String? = null,
    var redisHost: String? = null,
    var redisPort: Int? = null,
    var redisMasterHost: String? = null,
    var redisMasterPort: Int? = null,
    var redisMasterLink: Boolean? = null,
    var redisMasterLinkDownSince: String? = null,
    var redisOffset: Int? = null,
    var redisOffsetDiff: Int? = null,
    var redisMaster: Boolean? = null,
    var redisRole: String? = null,
    var redisRW: String? = null,
    var redisMode: String? = null,
    var redisStatus: String? = null,
    var redisSlaveCount: Int? = null,
    var redisClientCount: Int? = null
)

class InstancesBuilder {

    @Inject private lateinit var inventory: Inventory
    @Inject private lateinit var sshConnector: SshConnector
    @Inject private lateinit var monitoring: Monitoring

    @Inject private lateinit var projects: Projects
    @Inject private lateinit var scmConnector: ScmConnector

    fun getInstances(): List<Instance> {
        val instancesMap = HashMap<String, Instance>()
        val instancesList = ArrayList<Instance>()
        sshConnector.analyze()
        for (env in inventory.getEnvironments()) {
            for (service in env.services) {
                for (envInstance in service.instances) {
                    val id = env.id + "-" + envInstance.id
                    val instance = Instance(
                        id = id,
                        env = env.id,
                        type = service.type,
                        service = service.svc,
                        name = envInstance.id
                    )

                    applyInventory(instance, service, envInstance)

                    val project = if (service.project != null) projects.getProject(service.project) else null
                    if (project != null) {
                        applyScm(instance, scmConnector.getSvnDetails(project.svnRepo, project.svnUrl))
                    }

                    val sshDetails = sshConnector.getDetails(envInstance.host, envInstance.folder)
                    applySsh(instance, sshDetails)
                    instancesMap.put(id, instance)
                    instancesList.add(instance)
                }
            }
        }

        for (env in inventory.getEnvironments()) {
            val monitoring = this.monitoring.getFor(env.id)
            for ((instanceId, indicators) in monitoring.all) {
                val id = env.id + "-" + instanceId
                var instance = instancesMap[id]
                if (instance == null) {
                    instance = Instance(
                        id = id,
                        env = env.id,
                        type = indicators["type"] as String,
                        service = indicators["service"] as String,
                        name = instanceId,
                        unexpected = true
                    )
                    instancesMap.put(id, instance)
                    instancesList.add(instance)
                }
                applyMonitoring(instance, indicators)
                if (StringUtils.isNotBlank(instance.deployHost) && instance.deployHost != instance.host) {
                    instance.unexpectedHost = true
                }
            }
        }
        return instancesList
    }

    private fun applyInventory(instance: Instance, service: Service, envInstance: InvInstance) {
        instance.project = service.project
        instance.deployHost = envInstance.host
        instance.deployFolder = envInstance.folder
    }

    private fun applySsh(instance: Instance, sshDetails: SshDetails) {
        instance.deployVersion = sshDetails.deployedVersion
        instance.deployRevision = sshDetails.deployedRevision
        instance.confCommited = sshDetails.confCommitted
        instance.confUpToDate = sshDetails.confUpToDate
    }

    private fun applyScm(instance: Instance, scmDetails: ScmDetails) {
        instance.latestVersion = scmDetails.latest
        instance.latestRevision = scmDetails.latestRevision
        instance.releasedVersion = scmDetails.released
        instance.releasedRevision = scmDetails.releasedRevision
    }

    private fun applyMonitoring(instance: Instance, indicators: Map<String, Any>) {
        instance.pid = indicators["pid"] as String?
        instance.host = indicators["host"] as String?
        instance.version = indicators["version"] as String?
        instance.revision = indicators["revision"]?.toString()

        instance.cluster = indicators["cluster"] as Boolean? ?: false
        instance.participating = indicators["participating"] as Boolean? ?: false
        instance.leader = indicators["leader"] as Boolean? ?: isLive(indicators)

        instance.status = indicators["status"] as String?
        instance.jmx = indicators["jmx"] as Int?
        instance.busVersion = indicators["busVersion"] as String?
        instance.coreVersion = indicators["coreVersion"] as String?
        instance.quickfixPersistence = indicators["quickfixPersistence"] as String?
        instance.redisHost = indicators["redisHost"] as String?
        instance.redisPort = indicators["redisPort"] as Int?
        instance.redisMasterHost = indicators["redisMasterHost"] as String?
        instance.redisMasterPort = indicators["redisMasterPort"] as Int?
        instance.redisMasterLink = indicators["redisMasterLink"] as Boolean?
        instance.redisMasterLinkDownSince = indicators["redisMasterLinkDownSince"] as String?
        instance.redisOffset = indicators["redisOffset"] as Int?
        instance.redisOffsetDiff = indicators["redisOffsetDiff"] as Int?
        instance.redisMaster = indicators["redisMaster"] as Boolean?
        instance.redisRole = indicators["redisRole"] as String?
        instance.redisRW = indicators["redisRW"] as String?
        instance.redisMode = indicators["redisMode"] as String?
        instance.redisStatus = indicators["redisStatus"] as String?
        instance.redisSlaveCount = indicators["redisSlaveCount"] as Int?
        instance.redisClientCount = indicators["redisClientCount"] as Int?
    }

    // FIXME Easier to have it on the UI level
    private fun isLive(indicators: Map<String, Any>): Boolean {
        val timestamp = indicators["timestamp"] as LocalDateTime?
        return if (timestamp != null) LocalDateTime(DateTimeZone.UTC).minusSeconds(1).isAfter(timestamp) else true
    }

}

class InstancesApi {

    companion object {
        private val logger = LogManager.getLogger(InstancesApi::class.java)
    }

    val router: Router

    @Inject private lateinit var instancesBuilder: InstancesBuilder
    @Inject private lateinit var cachedScmConnector: CachedScmConnector
    @Inject private lateinit var projects: Projects
    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.router = Router.router(vertx)

        router.get("/").handler(this::instances)
        router.post("/reload").handler(this::reload)
    }

    fun instances(ctx: RoutingContext) {
        try {
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(instancesBuilder.getInstances()))
        } catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }

    }

    fun reload(ctx: RoutingContext) {
        try {
            logger.info("Reloading ...")
            // FIXME Reload

            for (project in projects.getProjects()) {
                if (StringUtils.isNotBlank(project.svnUrl)) {
                    cachedScmConnector.refresh(project.svnRepo, project.svnUrl)
                }
            }

            ctx.response().putHeader("content-type", "text/javascript").end()
        } catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }

    }
}