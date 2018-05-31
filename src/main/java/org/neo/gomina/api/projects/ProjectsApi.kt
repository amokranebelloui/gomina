package org.neo.gomina.api.projects

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.projects.ProjectDetail
import org.neo.gomina.model.project.Project
import org.neo.gomina.model.project.Projects
import org.neo.gomina.plugins.jenkins.JenkinsPlugin
import org.neo.gomina.plugins.project.apply
import org.neo.gomina.plugins.scm.ScmPlugin
import org.neo.gomina.plugins.sonar.SonarPlugin
import javax.inject.Inject

class ProjectsApi {

    companion object {
        private val logger = LogManager.getLogger(ProjectsApi::class.java)
    }

    val router: Router

    @Inject private lateinit var projects: Projects

    // FIXME Plugins
    @Inject lateinit private var scmPlugin: ScmPlugin
    @Inject lateinit private var sonarPlugin: SonarPlugin
    @Inject lateinit private var jenkinsPlugin: JenkinsPlugin

    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.router = Router.router(vertx)

        router.get("/").handler(this::projects)
        router.get("/:projectId").handler(this::project)
        router.get("/:projectId/doc/:docId").handler(this::projectDoc)

        router.post("/:projectId/reloadscm").handler(this::reloadProject)
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

    private fun projectDoc(ctx: RoutingContext) {
        val projectId = ctx.request().getParam("projectId")
        val docId = ctx.request().getParam("docId")
        try {
            logger.info("Get doc for $projectId $docId")
            val doc = scmPlugin.getDocument(projectId, docId) //.joinToString(separator = "")
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
        return projects.getProjects().map { build(it) }
    }

    private fun getProject(projectId: String): ProjectDetail? {
        return projects.getProject(projectId)?.let { build(it) }
    }

    private fun build(project: Project): ProjectDetail {
        return ProjectDetail(project.id).apply {
            apply(project)
            scmPlugin.enrich(project, this)
            sonarPlugin.enrich(project, this)
            jenkinsPlugin.enrich(project, this)
        }
    }

    private fun reloadProject(ctx: RoutingContext) {
        try {
            val projectId = ctx.request().getParam("projectId")
            logger.info("Reloading Project data $projectId ...")
            scmPlugin.reloadProject(projectId)
            jenkinsPlugin.reload(projectId) // FIXME Jenkins in it's own, or rename API
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot get project", e)
            ctx.fail(500)
        }
    }

}