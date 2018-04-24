package org.neo.gomina.plugins.ssh

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import javax.inject.Inject

class SshApi {

    companion object {
        private val logger = LogManager.getLogger(SshApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject lateinit var sshPlugin: SshPlugin

    private val mapper = ObjectMapper()

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.post("/:envId/reload").handler(this::reload)
    }

    fun reload(ctx: RoutingContext) {
        try {
            vertx.executeBlocking({future: Future<Void> ->
                val envId = ctx.request().getParam("envId")
                logger.info("Reloading SSH data $envId ...")
                sshPlugin.reloadInstances(envId)
                future.complete()
            }, false)
            {res: AsyncResult<Void> ->
                ctx.response().putHeader("content-type", "text/javascript").end("reload SSH done!")
            }
        }
        catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }
    }

}