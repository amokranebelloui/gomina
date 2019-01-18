package org.neo.gomina.api.work

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.name.Named
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.api.component.CommitDetail
import org.neo.gomina.api.component.CommitLogEnricher
import org.neo.gomina.integration.scm.ScmService
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.runtime.Topology
import org.neo.gomina.model.user.Users
import org.neo.gomina.model.work.Work
import org.neo.gomina.model.work.WorkList
import javax.inject.Inject

data class WorkDetail(val id: String, val label: String, val type: String?,
              val jira: String?, val jiraUrl: String?,
              val status: String,
              val people: List<String>, // TODO User Ref
              val components: List<String> = emptyList())

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

class WorkApi {

    companion object {
        private val logger = LogManager.getLogger(WorkApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject lateinit var workList: WorkList
    @Inject @Named("jira.url") lateinit var jiraUrl: String
    @Inject @Named("work.reference.env") lateinit var workReferenceEnv: String

    @Inject private lateinit var componentRepo: ComponentRepo
    @Inject private lateinit var scmService: ScmService
    @Inject private lateinit var commitLogEnricher: CommitLogEnricher
    @Inject private lateinit var topology: Topology
    @Inject private lateinit var users: Users

    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.get("/list").handler(this::workList)
        router.get("/detail/:workId").handler(this::workDetail)
        router.get("/detail").handler(this::workDetail)
    }

    private fun workList(ctx: RoutingContext) {
        try {
            logger.info("Get Work List")
            val workList = workList.getAll().map { it.map(jiraUrl) }
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(workList))
        }
        catch (e: Exception) {
            logger.error("Cannot get Work List", e)
            ctx.fail(500)
        }
    }

    private fun workDetail(ctx: RoutingContext) {
        val workId = ctx.request().getParam("workId")
        try {
            logger.info("Get Work Detail")

            val work = workId?.let { workList.get(it) }
            val detail = work?.components
                    ?.mapNotNull { componentRepo.get(it) }
                    ?.map { component ->
                        val commits = component.scm?.let {scm ->
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
                        ?: emptyList()
                        ComponentWorkDetail(component.id, commits)
                    }
                    ?: emptyList()
            val workDetail = work?.map(jiraUrl)
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

private fun Work.map(jiraUrl: String): WorkDetail {
    return WorkDetail(
            id = id,
            label = label,
            type = type,
            jira = jira,
            jiraUrl = jiraUrl
                    .takeIf { it.isNotBlank() && jira?.isNotBlank() ?: false }
                    ?.let { "$it/$jira" },
            status = status.toString(),
            people = people,
            components = components
    )
}
