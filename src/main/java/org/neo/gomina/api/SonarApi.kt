package org.neo.gomina.api

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.plugins.sonar.SonarPlugin
import javax.inject.Inject

class SonarApi {

    companion object {
        private val logger = LogManager.getLogger(SonarApi::class.java)
    }

    val vertx: Vertx
    val router: Router

    @Inject lateinit var sonarPlugin: SonarPlugin

    @Inject
    constructor(vertx: Vertx) {
        this.vertx = vertx
        this.router = Router.router(vertx)

        router.post("/reload").handler(this::reload)
    }

    fun reload(ctx: RoutingContext) {
        try {
            vertx.executeBlocking({future: Future<Void> ->
                //val envId = ctx.request().getParam("envId")
                logger.info("Reloading Sonar data ...")
                sonarPlugin.reload()
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