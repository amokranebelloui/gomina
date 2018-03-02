package org.neo.gomina.api.projects

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.project.Project
import org.neo.gomina.model.project.Projects
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.plugins.scm.ScmConnector
import org.neo.gomina.plugins.scm.ScmDetails
import org.neo.gomina.plugins.scm.impl.CachedScmConnector
import org.neo.gomina.model.sonar.SonarConnector
import org.neo.gomina.model.sonar.SonarIndicators
import java.util.*
import javax.inject.Inject


data class ProjectDetail (
    val id: String?,
    val label: String?,
    val type: String?,
    val repo: String?,
    val svn: String?,
    val mvn: String?,
    val jenkins: String?,
    val changes: Int?,
    val latest: String?,
    val released: String?,
    val loc: Double?,
    val coverage: Double?,
    val commitLog: List<CommitLogEntry>
)

data class CommitLogEntry (
    val revision: String?,
    val date: Date?,
    val author: String?,
    val message: String?
)

class ProjectsBuilder {

    companion object {
        private val logger = LogManager.getLogger(ProjectsBuilder::class.java)
    }

    @Inject private lateinit var projects: Projects
    @Inject private lateinit var scmConnector: ScmConnector
    @Inject private lateinit var sonarConnector: SonarConnector

    fun getProjects(): List<ProjectDetail> {
        val result = ArrayList<ProjectDetail>()
        val sonarIndicatorsMap = sonarConnector.metrics
        for (project in projects.getProjects()) {
            val scmDetails = scmConnector.getSvnDetails(project.svnRepo, project.svnUrl)
            val sonarIndicators = sonarIndicatorsMap[project.maven]
            val projectDetail = build(project, scmDetails, emptyList(), sonarIndicators)
            result.add(projectDetail)
        }
        return result
    }

    fun getProject(projectId: String): ProjectDetail? {
        val project = projects.getProject(projectId)
        if (project != null) {
            val sonarIndicators = sonarConnector.getMetrics(project.maven)[project.maven]
            val commitLog: List<CommitLogEntry> = map(scmConnector.getCommitLog(project.svnRepo, project.svnUrl))
            val scmDetails: ScmDetails = scmConnector.getSvnDetails(project.svnRepo, project.svnUrl)
            return build(project, scmDetails, commitLog, sonarIndicators)
        }
        return null
    }

    private fun map(commitLog: List<Commit>): List<CommitLogEntry> {
        return commitLog.map { CommitLogEntry(
                revision = it.revision,
                date = it.date,
                author = it.author,
                message = it.message
        ) }
    }

    private fun build(project: Project, scmDetails: ScmDetails?, commitLog: List<CommitLogEntry>, sonarIndicators: SonarIndicators?): ProjectDetail {
        return ProjectDetail(
                id = project.id,
                label = project.label ?: project.id,
                type = project.type,
                repo = project.svnRepo,
                svn = project.svnUrl,
                mvn = project.maven,
                jenkins = project.jenkinsJob,

                changes = scmDetails?.changes,
                latest = scmDetails?.latest,
                released = scmDetails?.released,

                loc = sonarIndicators?.loc,
                coverage = sonarIndicators?.coverage,

                commitLog = commitLog
        )
    }

}

class ProjectsApi {

    companion object {
        private val logger = LogManager.getLogger(ProjectsApi::class.java)
    }

    val router: Router

    @Inject private lateinit var projectBuilder: ProjectsBuilder
    @Inject private lateinit var cachedScmConnector: CachedScmConnector
    @Inject private lateinit var projects: Projects
    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.router = Router.router(vertx)

        router.get("/").handler(this::projects)
        router.get("/:projectId").handler(this::project)
        router.post("/:projectId/reload").handler(this::reload)
    }

    fun projects(ctx: RoutingContext) {
        try {
            ctx.response().putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(projectBuilder.getProjects()))
        } catch (e: Exception) {
            logger.error("Cannot get projects", e)
            ctx.fail(500)
        }
    }

    fun project(ctx: RoutingContext) {
        try {
            val projectId = ctx.request().getParam("projectId")
            val project = projectBuilder.getProject(projectId)
            if (project != null) {
                ctx.response().putHeader("content-type", "text/javascript")
                        .end(mapper.writeValueAsString(project))
            } else {
                logger.info("Cannot get project " + projectId)
                ctx.fail(404)
            }
        } catch (e: Exception) {
            logger.error("Cannot get project", e)
            ctx.fail(500)
        }
    }

    fun reload(ctx: RoutingContext) {
        try {
            val projectId = ctx.request().getParam("projectId")
            val project = projects.getProject(projectId)
            cachedScmConnector.refresh(project?.svnRepo ?: "", project?.svnUrl ?: "")
        } catch (e: Exception) {
            logger.error("Cannot get project", e)
            ctx.fail(500)
        }
    }
}