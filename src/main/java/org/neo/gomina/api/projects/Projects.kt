package org.neo.gomina.api.projects

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.name.Named
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.projects.ProjectDetail
import org.neo.gomina.core.projects.ProjectSet
import org.neo.gomina.core.projects.ProjectsExt
import org.neo.gomina.model.project.Projects
import java.util.*
import javax.inject.Inject

class ProjectsApi {

    companion object {
        private val logger = LogManager.getLogger(ProjectsApi::class.java)
    }

    val router: Router

    @Inject private lateinit var projects: Projects

    @Inject @Named("projects.plugins") lateinit var plugins: ArrayList<ProjectsExt>

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
        plugins.forEach { it.onGetProjects(projectSet) }
        return projectSet.list
    }

    private fun getProject(projectId: String): ProjectDetail? {
        val projectDetail = ProjectDetail(projectId)
        plugins.forEach { it.onGetProject(projectId, projectDetail) }
        return projectDetail
    }

    fun reload(ctx: RoutingContext) {
        try {
            val projectId = ctx.request().getParam("projectId")
            val project = projects.getProject(projectId)
            //scmPlugin.refresh(project?.svnRepo ?: "", project?.svnUrl ?: "")
            // FIXME Put back reload
        } catch (e: Exception) {
            logger.error("Cannot get project", e)
            ctx.fail(500)
        }
    }
}