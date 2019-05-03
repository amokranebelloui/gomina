package org.neo.gomina.api.component

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
import org.apache.logging.log4j.LogManager
import org.neo.gomina.api.common.UserRef
import org.neo.gomina.api.common.toDateUtc
import org.neo.gomina.api.common.toLocalDate
import org.neo.gomina.api.instances.VersionDetail
import org.neo.gomina.api.instances.toVersionDetail
import org.neo.gomina.api.work.IssueRef
import org.neo.gomina.integration.jenkins.JenkinsService
import org.neo.gomina.integration.scm.ScmService
import org.neo.gomina.integration.sonar.SonarService
import org.neo.gomina.model.component.Component
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.component.NewComponent
import org.neo.gomina.model.component.Scm
import org.neo.gomina.model.event.Events
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.runtime.ExtInstance
import org.neo.gomina.model.runtime.Topology
import org.neo.gomina.model.work.WorkList
import java.util.*
import javax.inject.Inject

data class ComponentRef(
        var id: String,
        var label: String? = null,
        var systems: List<String> = emptyList()
)

data class ComponentDetail(
        var id: String,
        var artifactId: String? = null,
        var label: String? = null,
        var type: String? = null,
        var inceptionDate: Date? = null,
        var owner: String? = null,
        var criticity: Int? = null,
        var systems: List<String> = emptyList(),
        var languages: List<String> = emptyList(),
        var tags: List<String> = emptyList(),
        var scmType: String? = null,
        var scmUrl: String? = null,
        var scmPath: String? = null,
        var scmLocation: String? = null,
        var hasMetadata: Boolean = false,
        var sonarServer: String? = null,
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
        var commitToRelease: Int? = null,
        var lastCommit: Date? = null,
        var commitActivity: Int? = null,
        var disabled: Boolean = false
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
        var issues: List<IssueRef> = emptyList(),

        val prodReleaseDate: Date?,
        val instances: List<InstanceRefDetail> = emptyList(),
        val deployments: List<InstanceRefDetail> = emptyList())

data class InstanceRefDetail(
        var id: String? = null,
        var env: String? = null,
        var name: String? = null,
        val running: VersionDetail?,
        val deployed: VersionDetail?)

data class NewComponentDetail(
        var id: String,
        var label: String? = null,
        var artifactId: String? = null,
        var type: String? = null,
        //var owner: String? = null,
        //var criticity: Int? = null,
        var systems: List<String> = emptyList(),
        var languages: List<String> = emptyList(),
        var tags: List<String> = emptyList(),
        var scmType: String? = null,
        var scmUrl: String? = null,
        var scmPath: String? = null,
        var hasMetadata: Boolean = false,
        var sonarServer: String? = null,
        var jenkinsServer: String? = null,
        var jenkinsJob: String? = null
)


class ComponentsApi {

    companion object {
        private val logger = LogManager.getLogger(ComponentsApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject private lateinit var componentRepo: ComponentRepo
    @Inject private lateinit var workList: WorkList
    @Inject private lateinit var inventory: Inventory
    @Inject private lateinit var events: Events

    @Inject private lateinit var scmService: ScmService
    @Inject private lateinit var commitLogEnricher: CommitLogEnricher
    @Inject private lateinit var sonarService: SonarService
    @Inject private lateinit var jenkinsService: JenkinsService

    @Inject private lateinit var topology: Topology

    private val mapper = ObjectMapper().registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.get("/").handler(this::components)
        router.get("/refs").handler(this::componentsRefs)

        router.get("/build/servers").handler(this::buildServers)
        router.get("/sonar/servers").handler(this::sonarServers)

        router.get("/:componentId").handler(this::component)
        router.get("/:componentId/scm").handler(this::commitLog)
        router.get("/:componentId/associated").handler(this::associated)
        router.get("/:componentId/doc/:docId").handler(this::componentDoc)

        router.post("/add").handler(this::addComponent)
        router.put("/reload-scm").handler(this::reloadScm)
        router.put("/reload-build").handler(this::reloadBuild)
        router.put("/reload-sonar").handler(this::reloadSonar)
        router.put("/:componentId/label").handler(this::editLabel)
        router.put("/:componentId/type").handler(this::editType)
        router.put("/:componentId/artifactId").handler(this::editArtifactId)
        router.put("/:componentId/inceptionDate").handler(this::editInceptionDate)
        router.put("/:componentId/owner").handler(this::editOwner)
        router.put("/:componentId/criticity").handler(this::editCriticity)
        router.put("/:componentId/scm").handler(this::editScm)
        router.put("/:componentId/sonar").handler(this::editSonar)
        router.put("/:componentId/build").handler(this::editBuild)
        router.put("/:componentId/add-system/:system").handler(this::addSystem)
        router.put("/:componentId/delete-system/:system").handler(this::deleteSystem)
        router.put("/:componentId/add-language/:language").handler(this::addLanguage)
        router.put("/:componentId/delete-language/:language").handler(this::deleteLanguage)
        router.put("/:componentId/add-tag/:tag").handler(this::addTag)
        router.put("/:componentId/delete-tag/:tag").handler(this::deleteTag)
        router.put("/:componentId/enable").handler(this::enable)
        router.put("/:componentId/disable").handler(this::disable)
        router.delete("/:componentId/delete").handler(this::delete)
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

    private fun componentsRefs(ctx: RoutingContext) {
        try {
            val components = this.componentRepo.getAll().mapNotNull { it.toComponentRef() }
            ctx.response().putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(components))
        } catch (e: Exception) {
            logger.error("Cannot get components", e)
            ctx.fail(500)
        }
    }

    fun buildServers(ctx: RoutingContext) {
        try {
            ctx.response().putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(jenkinsService.servers()))
        } catch (e: Exception) {
            logger.error("Cannot get Build servers", e)
            ctx.fail(500)
        }
    }

    fun sonarServers(ctx: RoutingContext) {
        try {
            ctx.response().putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(sonarService.servers()))
        } catch (e: Exception) {
            logger.error("Cannot get Sonar servers", e)
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
                val log = if (branch?.isNotBlank() == true) scmService.getBranch(it, branch)
                          else componentRepo.getCommitLog(componentId)
                val instances = topology.buildExtInstances(componentId)
                val prodEnvs = inventory.getProdEnvironments().map { it.id }
                val releases = events.releases(componentId, prodEnvs)
                commitLogEnricher.enrichLog(log, instances, releases)
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

                val mostActive = componentRepo.getAll()
                        .filter { it.shareSystem(component) }
                        .map { it to it.commitActivity }
                        .filter { (_, activity) -> activity > 0 }
                        .sortedBy { (_, activity) -> activity }
                        .take(7 - associated.size)
                        .map { (component, _) -> ComponentRef(component.id, component.label) }
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
                apply(component, sonarService, jenkinsService)
            }
        }
        catch (e: Exception) {
            logger.error("", e)
        }
        return null
    }

    private fun addComponent(ctx: RoutingContext) {
        val componentId = ctx.request().getParam("componentId")
        try {
            val newComp = mapper.readValue<NewComponentDetail>(ctx.body.toString())
            logger.info("Adding component $componentId => $newComp")
            componentRepo.add(NewComponent(
                    id = newComp.id,
                    label = newComp.label,
                    artifactId = newComp.artifactId,
                    type = newComp.type,
                    systems = newComp.systems,
                    tags = newComp.tags,
                    languages = newComp.languages,
                    scm = Scm(newComp.scmType ?: "", newComp.scmUrl ?: "", newComp.scmPath ?: ""), // FIXME ????
                    hasMetadata = newComp.hasMetadata,
                    jenkinsServer = newComp.jenkinsServer,
                    jenkinsJob = newComp.jenkinsJob,
                    sonarServer = newComp.sonarServer
            ))
            logger.info("Added component " + componentId)
            val component = this.componentRepo.get(componentId)?.let { this.build(it) }
            ctx.response().putHeader("content-type", "text/javascript").end(mapper.writeValueAsString(component))
        }
        catch (e: Exception) {
            logger.error("Cannot add component", e)
            ctx.response().putHeader("content-type", "text/javascript").setStatusCode(403).end(e.message)
        }
    }

    private fun reloadScm(ctx: RoutingContext) {
        try {
            ctx.request().getParam("componentIds")?.split(",")?.map { it.trim() }?.forEach {
                componentRepo.get(it)?.let { component ->
                    try {
                        logger.info("Reload SCM data for $it ...")
                        component.scm?.let { scmService.reloadScmDetails(component, it) }
                    }
                    catch (e: Exception) {
                        logger.error("Error Reloading SCM for ${component.id}", e)
                    }
                }
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
            ctx.request().getParam("componentIds")?.split(",")?.map { it.trim() }?.forEach {
                componentRepo.get(it)?.let { component ->
                    logger.info("Reload Jenkins data for $it ...")
                    jenkinsService.reload(component)
                }
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
                ctx.request().getParam("componentIds")?.split(",")?.map { it.trim() }?.forEach {
                    logger.info("Reloading Sonar data for $it ... [actually doing it for all components]")
                }
                // TODO Limit Sonar reload scope  to a component
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

    private fun editLabel(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            val label = ctx.request().getParam("label")
            logger.info("Edit Label $componentId $label ...")
            componentRepo.editLabel(componentId, label)
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot edit label", e)
            ctx.fail(500)
        }
    }

    private fun editType(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            val type = ctx.request().getParam("type")
            logger.info("Edit Type $componentId $type ...")
            componentRepo.editType(componentId, type)
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot edit type", e)
            ctx.fail(500)
        }
    }

    private fun editArtifactId(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            val artifactId = ctx.request().getParam("id")
            logger.info("Edit ArtifactId $componentId $artifactId ...")
            componentRepo.editArtifactId(componentId, artifactId)
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot edit artifactId", e)
            ctx.fail(500)
        }
    }

    private fun editInceptionDate(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            val inceptionDate = ctx.request().getParam("inceptionDate")
            logger.info("Edit InceptionDate $componentId $inceptionDate ...")
            componentRepo.editInceptionDate(componentId, inceptionDate?.toLocalDate())
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot edit artifactId", e)
            ctx.fail(500)
        }
    }

    private fun editOwner(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            val owner = ctx.request().getParam("owner")
            logger.info("Edit Owner $componentId $owner ...")
            componentRepo.editOwner(componentId, owner)
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot edit artifactId", e)
            ctx.fail(500)
        }
    }

    private fun editCriticity(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            val criticity = ctx.request().getParam("criticity")
            logger.info("Edit Criticity $componentId $criticity ...")
            componentRepo.editCriticity(componentId, criticity?.toInt())
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot edit artifactId", e)
            ctx.fail(500)
        }
    }

    private fun editScm(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            val type = ctx.request().getParam("type")
            val url = ctx.request().getParam("url")
            val path = ctx.request().getParam("path")
            val hasMetadata = ctx.request().getParam("hasMetadata")
            logger.info("Edit SCM $componentId $type $url $path $hasMetadata ...")
            componentRepo.editScm(componentId, type, url, path, hasMetadata?.toBoolean() ?: false)
            componentRepo.get(componentId)?.let { component ->
                logger.info("Reload SCM data for $componentId ...")
                component.scm?.let { scmService.reloadScmDetails(component, it) }
            }
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot edit SCM", e)
            ctx.fail(500)
        }
    }

    private fun editSonar(ctx: RoutingContext) {
        try {
            logger.info("Edit Sonar...")
            val componentId = ctx.request().getParam("componentId")
            val server = ctx.request().getParam("server")
            logger.info("Edit Sonar $componentId $server ...")
            componentRepo.editSonar(componentId, server)
            // TODO Reload sonar data
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot edit Sonar", e)
            ctx.fail(500)
        }
    }

    private fun editBuild(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            val server = ctx.request().getParam("server")
            val job = ctx.request().getParam("job")
            logger.info("Edit Buil $componentId $server $job ...")
            componentRepo.editBuild(componentId, server, job)
            componentRepo.get(componentId)?.let { component ->
                logger.info("Reload Jenkins data for $componentId ...")
                jenkinsService.reload(component)
            }
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot edit build", e)
            ctx.fail(500)
        }
    }

    private fun addSystem(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            val system = ctx.request().getParam("system")
            logger.info("Add system $componentId $system ...")
            componentRepo.addSystem(componentId, system)
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot add system", e)
            ctx.fail(500)
        }
    }

    private fun deleteSystem(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            val system = ctx.request().getParam("system")
            logger.info("Delete system $componentId $system ...")
            componentRepo.deleteSystem(componentId, system)
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot delete system", e)
            ctx.fail(500)
        }
    }

    private fun addLanguage(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            val language = ctx.request().getParam("language")
            logger.info("Add language $componentId $language ...")
            componentRepo.addLanguage(componentId, language)
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot add language", e)
            ctx.fail(500)
        }
    }

    private fun deleteLanguage(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            val language = ctx.request().getParam("language")
            logger.info("Delete language $componentId $language ...")
            componentRepo.deleteLanguage(componentId, language)
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot delete language", e)
            ctx.fail(500)
        }
    }

    private fun addTag(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            val tag = ctx.request().getParam("tag")
            logger.info("Add tag $componentId $tag ...")
            componentRepo.addTag(componentId, tag)
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot add tag", e)
            ctx.fail(500)
        }
    }

    private fun deleteTag(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            val tag = ctx.request().getParam("tag")
            logger.info("Delete tag $componentId $tag ...")
            componentRepo.deleteTag(componentId, tag)
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot delete tag", e)
            ctx.fail(500)
        }
    }

    private fun enable(ctx: RoutingContext) {
        try {
            val componentId = ctx.request().getParam("componentId")
            logger.info("Enable $componentId [TODO] ...")
            componentRepo.enable(componentId)
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
            logger.info("Disable $componentId [TODO] ...")
            componentRepo.disable(componentId)
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

private fun ComponentDetail.apply(component: Component, sonarService: SonarService, jenkinsService: JenkinsService) {
    this.artifactId = component.artifactId
    this.label = component.label ?: component.id
    this.type = component.type
    this.systems = component.systems
    this.languages = component.languages
    this.tags = component.tags
    this.scmType = component.scm?.type
    this.scmUrl = component.scm?.url
    this.scmPath = component.scm?.path
    this.scmLocation = component.scm?.fullUrl
    this.hasMetadata = component.hasMetadata
    this.sonarServer = component.sonarServer
    this.sonarUrl = sonarService.url(component)
    this.jenkinsServer = component.jenkinsServer
    this.jenkinsJob = component.jenkinsJob

    // SCM
    this.inceptionDate = component.inceptionDate?.toDateUtc
    this.owner = component.owner
    this.criticity = component.criticity
    this.branches = component.branches.map {
        BranchDetail(name = it.name, origin = it.origin, originRevision = it.originRevision)
    }
    this.docFiles = component.docFiles
    this.changes = component.changes
    this.latest = component.latest?.version
    this.released = component.released?.version
    this.lastCommit = component.lastCommit?.toDateUtc
    this.commitActivity = component.commitActivity
    this.commitToRelease = component.commitToRelease

    this.loc = component.loc
    this.coverage = component.coverage

    this.jenkinsUrl = jenkinsService.url(component)
    this.buildNumber = component.buildNumber
    this.buildStatus = if (component.buildBuilding == true) "BUILDING" else component.buildStatus
    this.buildTimestamp = component.buildTimestamp

    this.disabled = component.disabled
}

fun ExtInstance.toRef() = InstanceRefDetail(
        id = this.completeId, env = this.envId, name = this.instanceId,
        running = this.indicators?.version?.toVersionDetail(),
        deployed = this.instance?.let { it.deployedVersion?.toVersionDetail() }
)

fun Component.toComponentRef() = ComponentRef(this.id, this.label, this.systems)