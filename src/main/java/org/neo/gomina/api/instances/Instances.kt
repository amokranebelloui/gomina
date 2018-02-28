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
            for ((instanceId, indicators) in monitoring.getAll()) {
                val id = env.id + "-" + instanceId
                var instance = instancesMap[id]
                if (instance == null) {
                    instance = Instance(
                        id = id,
                        env = env.id,
                        type = indicators["type"],
                        service = indicators["service"],
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

    private fun applyMonitoring(instance: Instance, indicators: Map<String, String>) {
        instance.pid = indicators["pid"]
        instance.host = indicators["host"]
        instance.version = indicators["version"]
        instance.revision = indicators["revision"]

        instance.cluster = indicators["cluster"]?.toBoolean() ?: false
        instance.participating = indicators["participating"]?.toBoolean() ?: false
        instance.leader = indicators["leader"]?.toBoolean() ?: true // Historically we didn't have this field

        instance.status = indicators["status"]
        instance.jmx = indicators["jmx"]?.toInt()
        instance.busVersion = indicators["busVersion"]
        instance.coreVersion = indicators["coreVersion"]
        instance.quickfixPersistence = indicators["quickfixPersistence"]
        instance.redisHost = indicators["redisHost"]
        instance.redisPort = indicators["redisPort"]?.toInt()
        instance.redisMasterHost = indicators["redisMasterHost"]
        instance.redisMasterPort = indicators["redisMasterPort"]?.toInt()
        instance.redisMasterLink = indicators["redisMasterLink"]?.toBoolean()
        instance.redisMasterLinkDownSince = indicators["redisMasterLinkDownSince"]
        instance.redisOffset = indicators["redisOffset"]?.toInt()
        instance.redisOffsetDiff = indicators["redisOffsetDiff"]?.toInt()
        instance.redisMaster = indicators["redisMaster"]?.toBoolean()
        instance.redisRole = indicators["redisRole"]
        instance.redisRW = indicators["redisRW"]
        instance.redisMode = indicators["redisMode"]
        instance.redisStatus = indicators["redisStatus"]
        instance.redisSlaveCount = indicators["redisSlaveCount"]?.toInt()
        instance.redisClientCount = indicators["redisClientCount"]?.toInt()
    }

    // FIXME Easier to have it on the UI level
    private fun isLive(indicators: Map<String, Any>): Boolean {
        val timestamp = indicators["timestamp"] as LocalDateTime?  // FIXME Date format
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