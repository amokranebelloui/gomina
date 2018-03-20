package org.neo.gomina.web

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.util.Modules
import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.StaticHandler
import org.apache.logging.log4j.LogManager
import org.neo.gomina.api.diagram.DiagramApi
import org.neo.gomina.api.envs.EnvsApi
import org.neo.gomina.api.instances.InstancesApi
import org.neo.gomina.api.projects.ProjectsApi
import org.neo.gomina.api.realtime.NotificationsApi
import org.neo.gomina.module.GominaModule

class WebVerticle : AbstractVerticle() {

    @Throws(Exception::class)
    override fun start() {
        logger.info("Starting...")

        val injector = Guice.createInjector(Modules.combine(GominaModule(), object : AbstractModule() {
            override fun configure() {
                bind(Vertx::class.java).toInstance(vertx)
            }
        }))

        val envsApi = injector.getInstance(EnvsApi::class.java)
        val projectsApi = injector.getInstance(ProjectsApi::class.java)
        val instancesApi = injector.getInstance(InstancesApi::class.java)
        val diagramApi = injector.getInstance(DiagramApi::class.java)
        val notificationsApi = injector.getInstance(NotificationsApi::class.java)

        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create())

        notificationsApi.start()

        router
                .mountSubRouter("/data/envs", envsApi.router)
                .mountSubRouter("/data/projects", projectsApi.router)
                .mountSubRouter("/data/instances", instancesApi.router)
                .mountSubRouter("/data/diagram", diagramApi.router)
                .mountSubRouter("/realtime", notificationsApi.router)

        router.get("/*").pathRegex(".*\\.(js|ico|map)").handler(StaticHandler.create("dist").setCachingEnabled(false))

        router.route().pathRegex("/.*").handler { ctx ->
            val response = ctx.response()
            response.putHeader("content-type", "text/html")
            try {
                val root = ctx.request().getParam("param0")
                logger.info("root " + root)
                val filename = "dist/index.html"
                response.sendFile(filename)
            }
            catch (e: Exception) {
                logger.error("Error serving static data", e)
                ctx.fail(404)
            }
        }

        router.get().failureHandler { ctx ->
            logger.info("Handling failure {}", ctx.statusCode())
            ctx.response().setStatusCode(ctx.statusCode()).end("Ooops! something went wrong")
        }

        vertx.createHttpServer().requestHandler { router.accept(it) } .listen(8080)
    }

    override fun stop() {
        logger.info("Stopping...")
    }

    companion object {
        private val logger = LogManager.getLogger(WebVerticle::class.java)
    }
}