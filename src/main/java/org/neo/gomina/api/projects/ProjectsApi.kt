package org.neo.gomina.api.projects

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.api.common.UserRef
import org.neo.gomina.api.common.toRef
import org.neo.gomina.api.instances.VersionDetail
import org.neo.gomina.integration.jenkins.JenkinsService
import org.neo.gomina.integration.jenkins.jenkins.BuildStatus
import org.neo.gomina.integration.scm.ScmService
import org.neo.gomina.integration.sonar.SonarIndicators
import org.neo.gomina.integration.sonar.SonarService
import org.neo.gomina.model.project.Project
import org.neo.gomina.model.project.Projects
import org.neo.gomina.model.project.Systems
import org.neo.gomina.model.runtime.ExtInstance
import org.neo.gomina.model.runtime.Topology
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.scm.ScmDetails
import org.neo.gomina.model.scm.activity
import org.neo.gomina.model.user.Users
import org.neo.gomina.model.version.Version
import org.neo.gomina.model.work.WorkList
import java.time.Clock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

data class ProjectRef (
        var id: String,
        var label: String? = null
)

data class ProjectDetail (
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

data class InstanceRefDetail(
        var id: String? = null,
        var env: String? = null,
        var name: String? = null,
        val running: VersionDetail?,
        val deployed: VersionDetail?)

data class CommitDetail(
        val revision: String?,
        var date: Date? = null,
        var author: UserRef? = null,
        var message: String? = null,

        var version: String? = null,

        val instances: List<InstanceRefDetail> = emptyList(),
        val deployments: List<InstanceRefDetail> = emptyList())


class ProjectsApi {

    companion object {
        private val logger = LogManager.getLogger(ProjectsApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject private lateinit var projects: Projects
    @Inject private lateinit var systems: Systems
    @Inject private lateinit var users: Users
    @Inject private lateinit var workList: WorkList

    @Inject private lateinit var scmService: ScmService
    @Inject private lateinit var sonarService: SonarService
    @Inject private lateinit var jenkinsService: JenkinsService

    @Inject private lateinit var topology: Topology

    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.get("/").handler(this::projects)
        router.get("/systems").handler(this::systems)
        router.get("/:projectId").handler(this::project)
        router.get("/:projectId/scm").handler(this::commitLog)
        router.get("/:projectId/associated").handler(this::associated)
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

    private fun commitLog(ctx: RoutingContext) {
        val projectId = ctx.request().getParam("projectId")
        val branch = ctx.request().getParam("branchId")
        try {
            logger.info("Get SCM log for project:$projectId branch:$branch")

            var log = projects.getProject(projectId)?.scm?.let {
                val log = if (branch?.isNotBlank() == true) scmService.getBranch(it, branch) else scmService.getTrunk(it)
                enrichLog(log, topology.buildExtInstances(projectId))
            }
            if (log != null) {
                ctx.response().putHeader("content-type", "text/html")
                        .end(mapper.writeValueAsString(log))
            }
            else {
                logger.info("Cannot get SCM log project:$projectId branch:$branch")
                ctx.fail(404)
            }
        } catch (e: Exception) {
            logger.error("Cannot get SCM log project:$projectId branch:$branch")
            ctx.fail(500)
        }
    }

    private fun enrichLog(log: List<Commit>, instances: List<ExtInstance>): CommitLogDetail {
        val tmp = log.map { Triple(it, mutableListOf<ExtInstance>(), mutableListOf<ExtInstance>()) }
        val unresolved = mutableListOf<ExtInstance>()
        instances.forEach { instance ->
            val runningVersion = instance.indicators?.version
            val deployedVersion = instance.sshDetails?.version
            val commitR = tmp.find { item -> runningVersion?.let { item.first.match(it) } == true }
            val commitD = tmp.find { item -> deployedVersion?.let { item.first.match(it) } == true }

            if (commitR == null && commitD == null) {
                unresolved.add(instance)
            }
            else {
                commitR?.let { it.second.add(instance) }
                commitD?.let { it.third.add(instance) }
            }
        }
        return CommitLogDetail(
                log = tmp.map { (commit, running, deployed) -> CommitDetail(
                        revision = commit.revision,
                        date = commit.date,
                        author = commit.author?.let { users.findForAccount(it) }?.toRef() ?: commit.author?.let { UserRef(shortName = commit.author) },
                        message = commit.message,
                        version = commit.release ?: commit.newVersion,
                        instances = running.map { it.toRef() },
                        deployments = deployed.map { it.toRef() }
                ) },
                unresolved = unresolved.map { it.toRef() }
        )

        /*
        val result = log.map {
            CommitLogEntry(
                    revision = it.revision,
                    date = it.date,
                    author = it.author,
                    message = it.message,
                    version = it.release ?: it.newVersion
            )
        }//.toMutableList()

        fun match(commit: CommitLogEntry, version: Version): Boolean {
            return commit.revision == version.revision ||
                    commit.version?.let { Version.isStable(it) && commit.version == version.version } == true
        }

        instances.forEach { instance ->
            val runningVersion = instance.indicators?.version
            val deployedVersion = instance.sshDetails?.version
            val commitR = result.find { commit -> runningVersion?.let { match(commit, it) } == true }
            val commitD = result.find { commit -> deployedVersion?.let { match(commit, it) } == true }

            if (commitR == null && commitD == null) {
                logger.info("@@@@ Cannot link ${instance.completeId}")
                val indexOfFirstR = result.indexOfFirst { it.revision != null && runningVersion != null && it.revision < runningVersion?.revision ?: "" }
                if (indexOfFirstR > 0) {
                    result.add(indexOfFirstR, CommitLogEntry(revision = runningVersion?.revision, instances = mutableListOf(instance.toRef())))
                }
                val indexOfFirstD = result.indexOfFirst { it.revision != null && deployedVersion != null && it.revision < deployedVersion?.revision ?: "" }
                if (indexOfFirstD > 0) {
                    result.add(indexOfFirstD, CommitLogEntry(revision = deployedVersion?.revision, deployments = mutableListOf(instance.toRef())))
                }
            }
            else {
                commitR?.let { it.instances.add(instance.toRef()) }
                commitD?.let { it.deployments.add(instance.toRef()) }
            }
        }
        return result
*/
    }

    private fun associated(ctx: RoutingContext) {
        val projectId = ctx.request().getParam("projectId")
        try {
            logger.info("Get projects associated to project:$projectId")

            val other = projects.getProject(projectId)?.let { project ->

                val associated = workList.getAll()
                        .filter { it.status.isOpen() && it.projects.contains(projectId) }
                        .flatMap { it.projects }
                        .toSet()
                        .mapNotNull { projects.getProject(it) }
                        .map { ProjectRef(it.id, it.label) }

                val now = LocalDateTime.now(Clock.systemUTC())
                val mostActive = projects.getProjects()
                        .filter { it.shareSystem(project) }
                        .mapNotNull { project -> project.scm?.let { project to (scmService.getScmDetails(it)?.commitLog?.activity(now) ?: 0) } }
                        .filter { (project, activity) -> activity > 0 }
                        .sortedBy { (project, activity) -> activity }
                        .take(7 - associated.size)
                        .map { (project, activity) -> ProjectRef(project.id, project.label) }
                associated union mostActive
            }

            ctx.response().putHeader("content-type", "text/html")
                        .end(mapper.writeValueAsString(other))
        } catch (e: Exception) {
            logger.error("Cannot projects associated to project project:$projectId")
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
                doc = it.scm?.let { scmService.getDocument(it, docId) } //.joinToString(separator = "")
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
                apply(project)
                project.scm
                        ?.let { scmService.getScmDetails(it) }
                        ?.let { apply(it) }
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
                project.scm?.let { scmService.reloadScmDetails(it) }
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

private fun ProjectDetail.apply(project: Project) {
    this.label = project.label ?: project.id
    this.type = project.type
    this.systems = project.systems
    this.languages = project.languages
    this.tags = project.tags
    this.scmType = project.scm?.type
    this.scmLocation = project.scm?.fullUrl
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

private fun ExtInstance.toRef() = InstanceRefDetail(
        id = this.completeId, env = this.envId, name = this.instanceId,
        running = this.indicators?.version?.toVersionDetail(),
        deployed = this.sshDetails?.let { VersionDetail(it.deployedVersion ?: "", it.deployedRevision ?: "") }
)

private fun Version.toVersionDetail() = VersionDetail(version = this.version, revision = this.revision)
