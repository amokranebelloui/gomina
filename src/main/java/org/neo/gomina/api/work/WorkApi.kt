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
import org.neo.gomina.integration.scm.ScmService
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.runtime.Topology
import org.neo.gomina.model.user.Users
import org.neo.gomina.model.work.Work
import org.neo.gomina.model.work.WorkList
import java.util.*
import javax.inject.Inject

data class WorkDetail(val id: String, val label: String, val type: String?,
                      val issues: List<IssueRef>, 
                      val status: String,
                      val people: List<UserRef>,
                      val components: List<ComponentRef> = emptyList(),
                      var creationDate: Date? = null,
                      var dueDate: String? = null,
                      val archived: Boolean
)

data class IssueRef(val issue: String, val issueUrl: String?)

data class WorkManifestDetail(val work: WorkDetail?,
                              val details: List<ComponentWorkDetail> = emptyList())

data class ComponentWorkDetail(val componentId: String, val commits: List<CommitDetail>)

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
    @Inject private lateinit var scmService: ScmService
    @Inject private lateinit var commitLogEnricher: CommitLogEnricher
    @Inject private lateinit var topology: Topology
    @Inject private lateinit var users: Users

    private val mapper = ObjectMapper().registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.get("/list").handler(this::workList)
        router.post("/add").handler(this::add)
        router.put("/:workId/update").handler(this::update)
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
                        it.components.mapNotNull { componentMap[it] }
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
        try {
            logger.info("Get Work Detail")

            val userMap = users.getUsers().map { it.toRef() }.associateBy { it.id }
            val componentMap = componentRepo.getAll().map { it.toComponentRef() }.associateBy { it.id }

            val work = workId?.let { workList.get(it) }
            val detail = work?.components
                    ?.mapNotNull { componentRepo.get(it) }
                    ?.map { component ->
                        val commits = try {
                            component.scm?.let {scm ->
                                scmService.getTrunk(scm)
                                        .let { log -> commitLogEnricher.enrichLog(log, topology.buildExtInstances(component.id)).log }
                                        .takeWhile { commit ->
                                            !commit.instances.map { it.env }.contains(workReferenceEnv) &&
                                            !commit.deployments.map { it.env }.contains(workReferenceEnv)
                                        }
                                /*
                                .map { commit ->
                                    CommitDetail(
                                        revision = commit.revision,
                                        date = commit.date,
                                        author = commit.author?.let { users.findForAccount(it) }?.toRef()
                                                ?: commit.author?.let { UserRef(shortName = commit.author) },
                                        message = commit.message,
                                        version = commit.release ?: commit.newVersion
                                    )

                                }*/
                            }
                        }
                        catch (e: Exception) {
                            logger.error("", e)
                            null
                        }
                        ComponentWorkDetail(component.id, commits ?: emptyList())
                    }
                    ?: emptyList()
            val workDetail = work?.toWorkDetail(issueTrackerUrl,
                    work.people.mapNotNull { userMap[it] },
                    work.components.mapNotNull { componentMap[it] }
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

private fun Work.toWorkDetail(issueTrackerUrl: String, people: List<UserRef>, components: List<ComponentRef>): WorkDetail {
    val url = if (issueTrackerUrl.isNotBlank()) issueTrackerUrl else null
    return WorkDetail(
            id = id,
            label = label,
            type = type,
            issues = issues.map { issue -> IssueRef(issue, url?.let { "$it/$issue" }) },
            status = status.toString(),
            people = people,
            components = components,
            creationDate = creationDate?.toDateUtc,
            dueDate = dueDate?.toString,
            archived = archived
    )
}
