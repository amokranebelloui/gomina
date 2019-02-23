package org.neo.gomina.api.component

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.api.common.UserRef
import org.neo.gomina.api.instances.VersionDetail
import org.neo.gomina.integration.jenkins.JenkinsService
import org.neo.gomina.integration.jenkins.jenkins.BuildStatus
import org.neo.gomina.integration.scm.ScmService
import org.neo.gomina.integration.sonar.SonarIndicators
import org.neo.gomina.integration.sonar.SonarService
import org.neo.gomina.model.component.Component
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.system.Systems
import org.neo.gomina.model.runtime.ExtInstance
import org.neo.gomina.model.runtime.Topology
import org.neo.gomina.model.scm.ScmDetails
import org.neo.gomina.model.scm.activity
import org.neo.gomina.model.user.Users
import org.neo.gomina.model.version.Version
import org.neo.gomina.model.work.WorkList
import java.time.Clock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

data class ComponentRef(
        var id: String,
        var label: String? = null
)

data class ComponentDetail(
        var id: String,
        var label: String? = null,
        var type: String? = null,
        var owner: String? = null,
        var critical: Int? = null,
        var systems: List<String> = emptyList(),
        var languages: List<String> = emptyList(),
        var tags: List<String> = emptyList(),
        var scmType: String? = null,
        var scmLocation: String? = null,
        var mvn: String? = null,
        var sonarUrl: String? = null,
        var jenkinsServer: String? = null,
        var jenkinsJob: String? = null,
        var jenkinsUrl: String? = null,
        var buildNumber: String? = null,
        var buildStatus: String? = null,
        var buildTimestamp: Long? = null,
        var branches: List<BranchDetail> = emptyList(),
        var docFiles: List<String> = emptyList(),
        var changes: Int? = null,
        var latest: String? = null,
        var released: String? = null,
        var loc: Double? = null,
        var coverage: Double? = null,
        var lastCommit: Date? = null,
        var commitActivity: Int? = null
)

data class BranchDetail (
        var name: String,
        var origin: String? = null,
        var originRevision: String? = null
)

data class CommitLogDetail(
        val log: List<CommitDetail>,
        val unresolved: List<InstanceRefDetail>)

data class CommitDetail(
        val revision: String?,
        var date: Date? = null,
        var author: UserRef? = null,
        var message: String? = null,

        var version: String? = null,

        val instances: List<InstanceRefDetail> = emptyList(),
        val deployments: List<InstanceRefDetail> = emptyList())

data class InstanceRefDetail(
        var id: String? = null,
        var env: String? = null,
        var name: String? = null,
        val running: VersionDetail?,
        val deployed: VersionDetail?)

class ComponentsApi {

    companion object {
        private val logger = LogManager.getLogger(ComponentsApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject private lateinit var componentRepo: ComponentRepo
    @Inject private lateinit var systems: Systems
    @Inject private lateinit var users: Users
    @Inject private lateinit var workList: WorkList

    @Inject private lateinit var scmService: ScmService
    @Inject private lateinit var commitLogEnricher: CommitLogEnricher
    @Inject private lateinit var sonarService: SonarService
    @Inject private lateinit var jenkinsService: JenkinsService

    @Inject private lateinit var topology: Topology

    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.get("/").handler(this::components)
        router.get("/systems").handler(this::systems)
        router.get("/:componentId").handler(this::component)
        router.get("/:componentId/scm").handler(this::commitLog)
        router.get("/:componentId/associated").handler(this::associated)
        router.get("/:componentId/doc/:docId").handler(this::componentDoc)

        router.post("/add").handler(this::addComponent)
        router.post("/:componentId/reload-scm").handler(this::reloadScm)
        router.post("/:componentId/reload-build").handler(this::reloadBuild)
        router.put("/:componentId/enable").handler(this::enable)
        router.put("/:componentId/disable").handler(this::disable)
        router.delete("/:componentId/delete").handler(this::delete)
        router.post("/reload-sonar").handler(this::reloadSonar)
    }

    fun components(ctx: RoutingContext) {
        try {
            val components = this.componentRepo.getAll().mapNotNull { this.build(it) }
            ctx.response().putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(components))
        } catch (e: Exception) {
            logger.error("Cannot get components", e)
            ctx.fail(500)
        }
    }

    fun systems(ctx: RoutingContext) {
        try {
            ctx.response().putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(systems.getSystems()))
        } catch (e: Exception) {
            logger.error("Cannot get components", e)
            ctx.fail(500)
        }
    }

    fun component(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            val component = this.componentRepo.get(componentId)?.let { this.build(it) }
            if (component != null) {
                ctx.response().putHeader("content-type", "text/javascript")
                        .end(mapper.writeValueAsString(component))
            } else {
                logger.info("Cannot get component " + componentId)
                ctx.fail(404)
            }
        } catch (e: Exception) {
            logger.error("Cannot get component", e)
            ctx.fail(500)
        }
    }

    private fun commitLog(ctx: RoutingContext) {
        val componentId = ctx.request().getParam("componentId")
        val branch = ctx.request().getParam("branchId")
        try {
            logger.info("Get SCM log for component:$componentId branch:$branch")

            var log = componentRepo.get(componentId)?.scm?.let {
                val log = if (branch?.isNotBlank() == true) scmService.getBranch(it, branch) else scmService.getTrunk(it)
                commitLogEnricher.enrichLog(log, topology.buildExtInstances(componentId))
            }
            if (log != null) {
                ctx.response().putHeader("content-type", "text/html")
                        .end(mapper.writeValueAsString(log))
            }
            else {
                logger.info("Cannot get SCM log component:$componentId branch:$branch")
                ctx.fail(404)
            }
        } catch (e: Exception) {
            logger.error("Cannot get SCM log component:$componentId branch:$branch")
            ctx.fail(500)
        }
    }

    private fun associated(ctx: RoutingContext) {
        val componentId = ctx.request().getParam("componentId")
        try {
            logger.info("Get components associated to component:$componentId")

            val other = componentRepo.get(componentId)?.let { component ->

                val associated = workList.getAll()
                        .filter { it.status.isOpen() && it.components.contains(componentId) }
                        .flatMap { it.components }
                        .toSet()
                        .mapNotNull { componentRepo.get(it) }
                        .map { ComponentRef(it.id, it.label) }

                val now = LocalDateTime.now(Clock.systemUTC())
                val mostActive = componentRepo.getAll()
                        .filter { it.shareSystem(component) }
                        .mapNotNull { component -> component.scm?.let { component to (scmService.getScmDetails(it)?.commitLog?.activity(now) ?: 0) } }
                        .filter { (component, activity) -> activity > 0 }
                        .sortedBy { (component, activity) -> activity }
                        .take(7 - associated.size)
                        .map { (component, activity) -> ComponentRef(component.id, component.label) }
                associated union mostActive
            }

            ctx.response().putHeader("content-type", "text/html")
                        .end(mapper.writeValueAsString(other))
        } catch (e: Exception) {
            logger.error("Cannot component associated to component:$componentId")
            ctx.fail(500)
        }
    }

    private fun componentDoc(ctx: RoutingContext) {
        val componentId = ctx.request().getParam("componentId")
        val docId = ctx.request().getParam("docId")
        try {
            logger.info("Get doc for $componentId $docId")
            var doc: String? = null
            componentRepo.get(componentId)?.let {
                doc = it.scm?.let { scmService.getDocument(it, docId) } //.joinToString(separator = "")
            }
            if (doc != null) {
                ctx.response().putHeader("content-type", "text/html")
                        .end(doc)
            } else {
                logger.info("Cannot get doc $componentId $docId")
                ctx.fail(404)
            }
        } catch (e: Exception) {
            logger.error("Cannot get doc $componentId $docId")
            ctx.fail(500)
        }
    }

    private fun build(component: Component): ComponentDetail? {
        try {
            return ComponentDetail(component.id).apply {
                apply(component)
                component.scm
                        ?.let { scmService.getScmDetails(it) }
                        ?.let { apply(it) }
                sonarService.getSonar(component, fromCache = true)?.let { apply(it) }
                jenkinsService.getStatus(component, fromCache = true)?.let { apply(it) }
            }
        }
        catch (e: Exception) {
            logger.error("", e)
        }
        return null
    }

    @Deprecated("Dummy") private val components = mutableSetOf<String>()

    private fun addComponent(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")

            if (!components.contains(componentId)) {
                logger.info("Adding component " + componentId)
                Thread.sleep(2000)
                components.add(componentId)
                //val component = this.componentRepo.get(componentId)?.let { this.build(it) }
                val component = ComponentDetail(id = componentId, label = "Component $componentId") // FIXME Real impl
                logger.info("Added component " + componentId)
                ctx.response().putHeader("content-type", "text/javascript").end(mapper.writeValueAsString(component))
            }
            else {
                ctx.response().putHeader("content-type", "text/javascript").setStatusCode(403).end("$componentId already exists")
            }

        }
        catch (e: Exception) {
            logger.error("Cannot get component", e)
            ctx.fail(500)
        }
    }


    private fun reloadScm(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            componentRepo.get(componentId)?.let { component ->
                logger.info("Reload SCM data for $componentId ...")
                component.scm?.let { scmService.reloadScmDetails(it) }
            }
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot get component", e)
            ctx.fail(500)
        }
    }

    private fun reloadBuild(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            componentRepo.get(componentId)?.let { component ->
                logger.info("Reload Jenkins data for $componentId ...")
                jenkinsService.getStatus(component, fromCache = false)
            }
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot reload Jenkins", e)
            ctx.fail(500)
        }
    }

    private fun reloadSonar(ctx: RoutingContext) {
        try {
            vertx.executeBlocking({future: Future<Void> ->
                //val envId = ctx.request().getParam("envId")
                logger.info("Reloading Sonar data ...")
                componentRepo.getAll()
                        .map { it.sonarServer }
                        .distinct()
                        .forEach { sonarServer ->
                            sonarService.reload(sonarServer)
                        }

                future.complete()
            }, false)
            {res: AsyncResult<Void> ->
                ctx.response().putHeader("content-type", "text/javascript").end("reload Sonar done!")
            }
        }
        catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }
    }

    private fun enable(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            componentRepo.get(componentId)?.let { component ->
                logger.info("Enable $componentId [TODO] ...")
                // FIXME Implement
            }
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot enable component", e)
            ctx.fail(500)
        }
    }

    private fun disable(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            componentRepo.get(componentId)?.let { component ->
                logger.info("Disable $componentId [TODO] ...")
                // FIXME Implement
            }
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot disable component", e)
            ctx.fail(500)
        }
    }

    private fun delete(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            componentRepo.get(componentId)?.let { component ->
                logger.info("Delete $componentId [TODO] ...")
                // FIXME Implement
            }
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot delete component", e)
            ctx.fail(500)
        }
    }

}

private fun ComponentDetail.apply(component: Component) {
    this.label = component.label ?: component.id
    this.type = component.type
    this.systems = component.systems
    this.languages = component.languages
    this.tags = component.tags
    this.scmType = component.scm?.type
    this.scmLocation = component.scm?.fullUrl
    this.mvn = component.maven
    this.jenkinsServer = component.jenkinsServer
    this.jenkinsJob = component.jenkinsJob
}

private fun ComponentDetail.apply(scmDetails: ScmDetails) {
    this.owner = scmDetails.owner
    this.critical = scmDetails.critical
    if (!scmDetails.mavenId.isNullOrBlank()) {
        this.mvn = scmDetails.mavenId
    }
    this.branches = scmDetails.branches.map {
        BranchDetail(name = it.name, origin = it.origin, originRevision = it.originRevision)
    }
    this.docFiles = scmDetails.docFiles
    this.changes = scmDetails.changes
    this.latest = scmDetails.latest
    this.released = scmDetails.released
    this.lastCommit = scmDetails.commitLog?.firstOrNull()?.date
    try {
        val reference = LocalDateTime.now(Clock.systemUTC())
        this.commitActivity = scmDetails.commitLog.activity(reference)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun ComponentDetail.apply(sonarIndicators: SonarIndicators?) {
    this.sonarUrl = sonarIndicators?.sonarUrl
    this.loc = sonarIndicators?.loc
    this.coverage = sonarIndicators?.coverage
}

private fun ComponentDetail.apply(status: BuildStatus?) {
    this.jenkinsUrl = status?.url
    this.buildNumber = status?.id
    this.buildStatus = if (status?.building == true) "BUILDING" else status?.result
    this.buildTimestamp = status?.timestamp
}

fun ExtInstance.toRef() = InstanceRefDetail(
        id = this.completeId, env = this.envId, name = this.instanceId,
        running = this.indicators?.version?.toVersionDetail(),
        deployed = this.sshDetails?.let { VersionDetail(it.deployedVersion ?: "", it.deployedRevision ?: "") }
)

private fun Version.toVersionDetail() = VersionDetail(version = this.version, revision = this.revision)
