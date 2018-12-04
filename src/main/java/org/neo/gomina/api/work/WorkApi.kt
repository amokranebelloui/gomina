package org.neo.gomina.api.work

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.name.Named
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.work.Work
import org.neo.gomina.model.work.WorkList
import javax.inject.Inject

data class WorkDetail(val id: String, val label: String, val type: String?,
              val jira: String?, val jiraUrl: String?,
              val status: String,
              val people: List<String>, val components: List<String> = emptyList())


class WorkApi {

    companion object {
        private val logger = LogManager.getLogger(WorkApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject lateinit var workList: WorkList
    @Inject @Named("jira.url") lateinit var jiraUrl: String

    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.get("/").handler(this::workList)
    }

    private fun workList(ctx: RoutingContext) {
        try {
            logger.info("Get Work List")
            val hosts = workList.getAll().map { it.map(jiraUrl) }
            ctx.response()
                    .putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(hosts))
        }
        catch (e: Exception) {
            logger.error("Cannot get hosts", e)
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