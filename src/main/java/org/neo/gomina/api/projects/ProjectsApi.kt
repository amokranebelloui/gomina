package org.neo.gomina.api.projects

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.jenkins.JenkinsService
import org.neo.gomina.integration.jenkins.jenkins.BuildStatus
import org.neo.gomina.integration.scm.ScmDetails
import org.neo.gomina.integration.scm.ScmRepos
import org.neo.gomina.integration.scm.ScmService
import org.neo.gomina.integration.sonar.SonarIndicators
import org.neo.gomina.integration.sonar.SonarService
import org.neo.gomina.model.project.Project
import org.neo.gomina.model.project.Projects
import org.neo.gomina.model.project.Systems
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class ProjectsApi {

    companion object {
        private val logger = LogManager.getLogger(ProjectsApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject private lateinit var projects: Projects
    @Inject private lateinit var systems: Systems

    @Inject private lateinit var scmService: ScmService
    @Inject private lateinit var scmRepos: ScmRepos
    @Inject private lateinit var sonarService: SonarService
    @Inject private lateinit var jenkinsService: JenkinsService


    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.get("/").handler(this::projects)
        router.get("/systems").handler(this::systems)
        router.get("/:projectId").handler(this::project)
        router.get("/:projectId/scm").handler(this::projectScm)
        router.get("/:projectId/doc/:docId").handler(this::projectDoc)

        router.post("/:projectId/reload-scm").handler(this::reloadProject)
        router.post("/reload-sonar").handler(this::reloadSonar)
    }

    fun projects(ctx: RoutingContext) {
        try {
            ctx.response().putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(this.getProjects()))
        } catch (e: Exception) {
            logger.error("Cannot get projects", e)
            ctx.fail(500)
        }
    }

    fun systems(ctx: RoutingContext) {
        try {
            ctx.response().putHeader("content-type", "text/javascript")
                    .end(mapper.writeValueAsString(systems.getSystems()))
        } catch (e: Exception) {
            logger.error("Cannot get projects", e)
            ctx.fail(500)
        }
    }

    fun project(ctx: RoutingContext) {
        try {
            val projectId = ctx.request().getParam("projectId")
            val project = this.getProject(projectId)
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

    private fun projectScm(ctx: RoutingContext) {
        val projectId = ctx.request().getParam("projectId")
        val branch = ctx.request().getParam("branchId")
        try {
            logger.info("Get SCM log for $projectId $branch")
            var log: List<CommitLogEntry>? = null
            projects.getProject(projectId)?.let {
                log = scmService.getBranch(it, branch).map {
                    CommitLogEntry(
                            revision = it.revision,
                            date = it.date,
                            author = it.author,
                            message = it.message,
                            version = it.release ?: it.newVersion
                    )
                }
            }
            if (log != null) {
                ctx.response().putHeader("content-type", "text/html")
                        .end(mapper.writeValueAsString(log))
            }
            else {
                logger.info("Cannot get SCM log $projectId $branch")
                ctx.fail(404)
            }
        } catch (e: Exception) {
            logger.error("Cannot get SCM log $projectId $branch")
            ctx.fail(500)
        }
    }

    private fun projectDoc(ctx: RoutingContext) {
        val projectId = ctx.request().getParam("projectId")
        val docId = ctx.request().getParam("docId")
        try {
            logger.info("Get doc for $projectId $docId")
            var doc: String? = null
            projects.getProject(projectId)?.let {
                doc = scmService.getDocument(it, docId) //.joinToString(separator = "")
            }
            if (doc != null) {
                ctx.response().putHeader("content-type", "text/html")
                        .end(doc)
            } else {
                logger.info("Cannot get doc $projectId $docId")
                ctx.fail(404)
            }
        } catch (e: Exception) {
            logger.error("Cannot get doc $projectId $docId")
            ctx.fail(500)
        }
    }

    private fun getProjects(): Collection<ProjectDetail> {
        return projects.getProjects().mapNotNull { build(it) }
    }

    private fun getProject(projectId: String): ProjectDetail? {
        return projects.getProject(projectId)?.let { build(it) }
    }

    private fun build(project: Project): ProjectDetail? {
        try {
            return ProjectDetail(project.id).apply {
                apply(project, scmRepos)
                scmService.getScmDetails(project, fromCache = true)?.let { apply(it) }
                sonarService.getSonar(project, fromCache = true)?.let { apply(it) }
                jenkinsService.getStatus(project, fromCache = true)?.let { apply(it) }
            }
        }
        catch (e: Exception) {
            logger.error("", e)
        }
        return null
    }

    private fun reloadProject(ctx: RoutingContext) {
        try {
            val projectId = ctx.request().getParam("projectId")
            projects.getProject(projectId)?.let { project ->
                logger.info("Reload SCM data for $projectId ...")
                scmService.getScmDetails(project, fromCache = false)
                // FIXME Jenkins in it's own, or rename API
                logger.info("Reload Jenkins data for $projectId ...")
                jenkinsService.getStatus(project, fromCache = false)
            }
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot get project", e)
            ctx.fail(500)
        }
    }

    private fun reloadSonar(ctx: RoutingContext) {
        try {
            vertx.executeBlocking({future: Future<Void> ->
                //val envId = ctx.request().getParam("envId")
                logger.info("Reloading Sonar data ...")
                projects.getProjects()
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

}

private fun ProjectDetail.apply(project: Project, scmRepos: ScmRepos) {
    this.label = project.label ?: project.id
    this.type = project.type
    this.systems = project.systems
    this.languages = project.languages
    this.tags = project.tags
    this.scmType = project.scm?.type
    this.scmRepo = project.scm?.repo
    this.scmLocation = project.scm?.url
    this.mvn = project.maven
    this.jenkinsServer = project.jenkinsServer
    this.jenkinsJob = project.jenkinsJob
}

private fun ProjectDetail.apply(scmDetails: ScmDetails) {
    this.owner = scmDetails.owner
    this.critical = scmDetails.critical
    if (!scmDetails.mavenId.isNullOrBlank()) {
        this.mvn = scmDetails.mavenId
    }
    this.scmUrl = scmDetails.url
    this.branches = scmDetails.branches.map {
        BranchDetail(name = it.name, origin = it.origin, originRevision = it.originRevision)
    }
    this.docFiles = scmDetails.docFiles
    this.changes = scmDetails.changes
    this.latest = scmDetails.latest
    this.released = scmDetails.released
    this.commitLog = scmDetails.commitLog.map {
        CommitLogEntry(
                revision = it.revision,
                date = it.date,
                author = it.author,
                message = it.message,
                version = it.release ?: it.newVersion
        )
    }
    this.lastCommit = scmDetails.commitLog?.firstOrNull()?.date
    try {
        val sixMonthAgo = LocalDateTime.now(Clock.systemUTC()).minusMonths(6)
        val aMonthAgo = LocalDateTime.now(Clock.systemUTC()).minusMonths(1)
        val aWeekAgo = LocalDateTime.now(Clock.systemUTC()).minusWeeks(1)
        val aDayAgo = LocalDateTime.now(Clock.systemUTC()).minusDays(1)
        this.commitActivity = scmDetails.commitLog
                .mapNotNull { it.date }
                .mapNotNull { LocalDateTime.from(it.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()) }
                .filter { it.isAfter(sixMonthAgo) }
                .map {
                    when {
                        it.isAfter(aDayAgo) -> 7
                        it.isAfter(aWeekAgo) -> 5
                        it.isAfter(aMonthAgo) -> 3
                        it.isAfter(sixMonthAgo) -> 1
                        else -> 0
                    }
                }
                .sumBy { it }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun ProjectDetail.apply(sonarIndicators: SonarIndicators?) {
    this.sonarUrl = sonarIndicators?.sonarUrl
    this.loc = sonarIndicators?.loc
    this.coverage = sonarIndicators?.coverage
}

private fun ProjectDetail.apply(status: BuildStatus?) {
    this.jenkinsUrl = status?.url
    this.buildNumber = status?.id
    this.buildStatus = if (status?.building == true) "BUILDING" else status?.result
    this.buildTimestamp = status?.timestamp
}
