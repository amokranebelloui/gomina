package org.neo.gomina.api.projects

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.project.Projects
import org.neo.gomina.model.projects.ProjectDetail
import org.neo.gomina.model.projects.ProjectSet
import org.neo.gomina.plugins.project.ProjectPlugin
import org.neo.gomina.plugins.scm.ScmPlugin
import org.neo.gomina.plugins.sonar.SonarPlugin
import javax.inject.Inject

class ProjectsApi {

    companion object {
        private val logger = LogManager.getLogger(ProjectsApi::class.java)
    }

    val router: Router

    @Inject private lateinit var projects: Projects

    @Inject private lateinit var projectPlugin: ProjectPlugin
    @Inject private lateinit var sonarPlugin: SonarPlugin
    @Inject private lateinit var scmPlugin: ScmPlugin

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

    private fun getProjects(): List<ProjectDetail> {
        val projectSet = ProjectSet()
        projectPlugin.onGetProjects(projectSet)
        sonarPlugin.onGetProjects(projectSet)
        scmPlugin.onGetProjects(projectSet)
        return projectSet.list
    }

    private fun getProject(projectId: String): ProjectDetail? {

        val projectDetail = ProjectDetail(projectId)
        projectPlugin.onGetProject(projectId, projectDetail)
        sonarPlugin.onGetProject(projectId, projectDetail)
        scmPlugin.onGetProject(projectId, projectDetail)
        return projectDetail
    }

    fun reload(ctx: RoutingContext) {
        try {
            val projectId = ctx.request().getParam("projectId")
            val project = projects.getProject(projectId)
            scmPlugin.refresh(project?.svnRepo ?: "", project?.svnUrl ?: "")
        } catch (e: Exception) {
            logger.error("Cannot get project", e)
            ctx.fail(500)
        }
    }
}