package org.neo.gomina.api.work

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.inject.name.Named
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.api.common.*
import org.neo.gomina.api.component.CommitDetail
import org.neo.gomina.api.component.CommitLogEnricher
import org.neo.gomina.api.component.ComponentRef
import org.neo.gomina.api.component.toComponentRef
import org.neo.gomina.integration.jenkins.JenkinsService
import org.neo.gomina.integration.scm.impl.ScmClients
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.event.Events
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.runtime.Topology
import org.neo.gomina.model.user.Users
import org.neo.gomina.model.work.Work
import org.neo.gomina.model.work.WorkList
import org.neo.gomina.model.work.WorkStatus
import java.util.*
import javax.inject.Inject

data class WorkDetail(val id: String, val label: String, val type: String?,
                      val issues: List<IssueRef>, 
                      val missingIssues: List<IssueRef>,
                      val status: String,
                      val people: List<UserRef>,
                      val components: List<ComponentRef> = emptyList(),
                      val missingComponents: List<ComponentRef> = emptyList(),
                      var creationDate: Date? = null,
                      var dueDate: String? = null,
                      val archived: Boolean
)

data class WorkRef(val id: String, val label: String,
                   val components: List<ComponentRef> = emptyList()
)

data class IssueRef(val issue: String, val issueUrl: String?)

data class WorkManifestDetail(val work: WorkDetail?,
                              val details: List<ComponentWorkDetail> = emptyList())

data class ComponentWorkDetail(val componentId: String,
                               val componentLabel: String,
                               val scmType: String?,
                               val commits: List<CommitDetail>,
                               val upToDate: Boolean,
                               val notDeployed: Boolean,

                               var jenkinsServer: String? = null,
                               var jenkinsJob: String? = null,
                               var jenkinsUrl: String? = null,
                               var buildNumber: String? = null,
                               var buildStatus: String? = null,
                               var buildTimestamp: Long? = null

)

/*
data class CommitDetail(
        val revision: String?,
        var date: Date? = null,
        var author: UserRef? = null,
        var message: String? = null,

        var version: String? = null)
*/

data class WorkData(val label: String?,
                    val type: String?,
                    val issues: List<String> = emptyList(),
                    val people: List<String> = emptyList(),
                    val components: List<String> = emptyList(),
                    var dueDate: String? = null)


class WorkApi {

    companion object {
        private val logger = LogManager.getLogger(WorkApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject lateinit var workList: WorkList
    @Inject @Named("jira.url") lateinit var issueTrackerUrl: String
    @Inject @Named("work.reference.env") lateinit var workReferenceEnv: String

    @Inject private lateinit var componentRepo: ComponentRepo
    @Inject private lateinit var scmClients: ScmClients
    @Inject private lateinit var commitLogEnricher: CommitLogEnricher
    @Inject private lateinit var jenkinsService: JenkinsService
    @Inject private lateinit var inventory: Inventory
    @Inject private lateinit var topology: Topology
    @Inject private lateinit var users: Users
    @Inject private lateinit var events: Events

    private val mapper = ObjectMapper().registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.get("/list").handler(this::workList)
        router.get("/refs").handler(this::workRefs)
        router.post("/add").handler(this::add)
        router.put("/:workId/update").handler(this::update)
        router.put("/:workId/change-status/:status").handler(this::changeStatus)
        router.put("/:workId/archive").handler(this::archive)
        router.put("/:workId/unarchive").handler(this::unarchive)

        router.get("/detail/:workId").handler(this::workDetail)
        router.get("/detail").handler(this::workDetail)
    }

    private fun workList(ctx: RoutingContext) {
        try {
            logger.info("Get Work List")
            val userMap = users.getUsers().map { it.toRef() }.associateBy { it.id }
            val componentMap = componentRepo.getAll().map { it.toComponentRef() }.associateBy { it.id }
            val workList = workList.getAll().map {
                it.toWorkDetail(issueTrackerUrl,
                        it.people.mapNotNull { userMap[it] },
                        it.components.mapNotNull { componentMap[it] },
                        emptySet(), emptyList()
                )
            }
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(workList))
        }
        catch (e: Exception) {
            logger.error("Cannot get Work List", e)
            ctx.fail(500)
        }
    }

    private fun workRefs(ctx: RoutingContext) {
        try {
            logger.info("Get Work Refs")
            val componentMap = componentRepo.getAll().map { it.toComponentRef() }.associateBy { it.id }
            val workList = workList.getAll()
                    .filter { !it.archived }
                    .sortedByDescending { it.creationDate }
                    .map { it.toWorkRef(it.components.mapNotNull { componentMap[it] }) }
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(workList))
        }
        catch (e: Exception) {
            logger.error("Cannot get Work Refs", e)
            ctx.fail(500)
        }
    }

    private fun add(ctx: RoutingContext) {
        try {
            val data = mapper.readValue<WorkData>(ctx.body.toString())
            logger.info("Adding work")
            val workId = workList.addWork(data.label, data.type, data.issues, data.people, data.components, data.dueDate.toLocalDate())
            ctx.response().putHeader("content-type", "text/javascript").end(mapper.writeValueAsString(workId))
        }
        catch (e: Exception) {
            logger.error("Cannot add Work", e)
            ctx.fail(500)
        }
    }

    private fun update(ctx: RoutingContext) {
        val workId = ctx.request().getParam("workId")
        try {
            val data = mapper.readValue<WorkData>(ctx.body.toString())
            logger.info("Updating work $workId $data ${data.dueDate.toLocalDate()}")
            workList.updateWork(workId, data.label, data.type, data.issues, data.people, data.components, data.dueDate.toLocalDate())
            ctx.response().putHeader("content-type", "text/javascript").end(mapper.writeValueAsString(workId))
        }
        catch (e: Exception) {
            logger.error("Cannot update Work", e)
            ctx.fail(500)
        }
    }

    private fun changeStatus(ctx: RoutingContext) {
        val workId = ctx.request().getParam("workId")
        val status = WorkStatus.valueOf(ctx.request().getParam("status"))
        try {
            logger.info("Updating work status $workId $status")
            workList.changeStatus(workId, status)
            ctx.response().putHeader("content-type", "text/javascript").end(mapper.writeValueAsString(workId))
        }
        catch (e: Exception) {
            logger.error("Cannot update Work status", e)
            ctx.fail(500)
        }
    }

    private fun archive(ctx: RoutingContext) {
        val workId = ctx.request().getParam("workId")
        try {
            logger.info("Archiving work $workId")
            workList.archiveWork(workId)
            ctx.response().putHeader("content-type", "text/javascript").end(mapper.writeValueAsString(workId))
        }
        catch (e: Exception) {
            logger.error("Cannot archive Work", e)
            ctx.fail(500)
        }
    }

    private fun unarchive(ctx: RoutingContext) {
        val workId = ctx.request().getParam("workId")
        try {
            logger.info("Unarchive work $workId")
            workList.unarchiveWork(workId)
            ctx.response().putHeader("content-type", "text/javascript").end(mapper.writeValueAsString(workId))
        }
        catch (e: Exception) {
            logger.error("Cannot unarchive Work", e)
            ctx.fail(500)
        }
    }

    private fun workDetail(ctx: RoutingContext) {
        val workId = ctx.request().getParam("workId")
        val refEnv = ctx.request().getParam("refEnv")
        try {
            logger.info("Get Work Detail $workId $refEnv")

            val userMap = users.getUsers().map { it.toRef() }.associateBy { it.id }
            val allComponents = componentRepo.getAll()
            val deployed = inventory.getDeployedComponents(refEnv?.takeIf { it.isNotBlank() })
            logger.info("Deployed $deployed")
            val deployedComponent = allComponents.filter { !it.disabled }.filter { deployed.contains(it.id) }
            val componentMap = allComponents.map { it.toComponentRef() }.associateBy { it.id }

            val work = workId?.let { workList.get(it) }
            val components = work?.components?.mapNotNull { componentRepo.get(it) } ?: deployedComponent
            val environments = inventory.getEnvironments()
            val prodEnvs = inventory.getProdEnvironments().map { it.id }
            val accounts = users.getUsers()
                    .flatMap { user -> user.accounts.map { it to user } }
                    .associateBy({ (account, _) -> account }, {(_, user) -> user})
            val componentsIssues = mutableSetOf<IssueRef>()
            val detail = components
                    .mapNotNull { component ->
                        component.scm?.let {scm ->
                            try {
                                val trunk = scmClients.getClient(scm).getTrunk()
                                val commitLog = componentRepo.getCommitLog(component.id, trunk)
                                        .let { log ->
                                            val instances = topology.buildExtInstances(component, environments)
                                            val releases = events.releases(component.id, prodEnvs)
                                            commitLogEnricher.enrichLog(trunk, log, scm, accounts, instances, releases).log
                                        }
                                val last = commitLog.indexOfLast { commit ->
                                    (commit.instances + commit.deployments)
                                            .mapNotNull { it.env }
                                            .contains(refEnv)
                                }
                                val commits = if (last != -1) commitLog.subList(0, last + 1) else commitLog
                                componentsIssues.addAll(commits.flatMap { it.issues })
                                ComponentWorkDetail(component.id,
                                        componentLabel = component.label ?: component.id,
                                        scmType = component.scm?.type,
                                        commits = commits,
                                        upToDate = last == 0,
                                        notDeployed = last == -1,
                                        jenkinsServer = component.jenkinsServer,
                                        jenkinsJob = component.jenkinsJob,
                                        jenkinsUrl = jenkinsService.url(component),
                                        buildNumber = component.buildNumber,
                                        buildStatus = if (component.buildBuilding == true) "BUILDING" else component.buildStatus,
                                        buildTimestamp = component.buildTimestamp
                                )
                            }
                            catch (e: Exception) {
                                logger.error("", e)
                                null
                            }
                        }
                    }
            val issuesComponents: List<ComponentRef> = work?.issues
                    ?.flatMap { componentRepo.componentsForIssue(it) }
                    ?.toSet()
                    ?.mapNotNull { componentMap[it] }
                    ?: emptyList()
            val workDetail = work?.toWorkDetail(issueTrackerUrl,
                    work.people.mapNotNull { userMap[it] },
                    work.components.mapNotNull { componentMap[it] },
                    componentsIssues, issuesComponents
            )
            val manifest = WorkManifestDetail(workDetail, detail)
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(manifest))
        }
        catch (e: Exception) {
            logger.error("Cannot get Work Detail", e)
            ctx.fail(500)
        }
    }

}

private fun Work.toWorkDetail(issueTrackerUrl: String, people: List<UserRef>, components: List<ComponentRef>,
                              componentsIssues: Set<IssueRef>, issuesComponents: List<ComponentRef>): WorkDetail {
    return WorkDetail(
            id = id,
            label = label,
            type = type,
            issues = issues.map { it.toIssueRef(issueTrackerUrl) },
            missingIssues = componentsIssues.filter { !issues.contains(it.issue) },
            status = status.toString(),
            people = people,
            components = components,
            missingComponents = issuesComponents.filter { !components.contains(it) },
            creationDate = creationDate?.toDateUtc,
            dueDate = dueDate?.toString,
            archived = archived
    )
}

private fun Work.toWorkRef(components: List<ComponentRef>): WorkRef {
    return WorkRef(
            id = id,
            label = label,
            components = components
    )
}

fun String.toIssueRef(issueTrackerUrl: String): IssueRef {
    val url = if (issueTrackerUrl.isNotBlank()) issueTrackerUrl else null
    return IssueRef(this, url?.let { "$it/jira/browse/$this" })
}