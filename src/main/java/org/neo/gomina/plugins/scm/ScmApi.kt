package org.neo.gomina.plugins.scm

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import javax.inject.Inject

class ScmApi {

    companion object {
        private val logger = LogManager.getLogger(ScmApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject lateinit var scmPlugin: ScmPlugin

    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.post("/:envId/reload").handler(this::reload)
        router.post("/project/:projectId/reload").handler(this::reloadProject)
    }

    private fun reload(ctx: RoutingContext) {
        try {
            vertx.executeBlocking({future: Future<Void> ->
                val envId = ctx.request().getParam("envId")
                logger.info("Reloading SCM data $envId ...")
                scmPlugin.reloadInstances(envId)
                future.complete()
            }, false)
            {res: AsyncResult<Void> ->
                ctx.response().putHeader("content-type", "text/javascript").end("reload SCM done!")
            }
        }
        catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }
    }

    private fun reloadProject(ctx: RoutingContext) {
        try {
            val projectId = ctx.request().getParam("projectId")
            logger.info("Reloading Project data $projectId ...")
            scmPlugin.reloadProject(projectId)
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot get project", e)
            ctx.fail(500)
        }
    }
}