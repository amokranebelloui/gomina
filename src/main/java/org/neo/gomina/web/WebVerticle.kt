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
import org.neo.gomina.api.auth.AuthApi
import org.neo.gomina.api.dependencies.DependenciesApi
import org.neo.gomina.api.diagram.DiagramApi
import org.neo.gomina.api.envs.EnvsApi
import org.neo.gomina.api.events.EventsApi
import org.neo.gomina.api.hosts.HostsApi
import org.neo.gomina.api.instances.InstancesApi
import org.neo.gomina.api.projects.ProjectsApi
import org.neo.gomina.api.realtime.NotificationsApi
import org.neo.gomina.api.users.UsersApi
import org.neo.gomina.api.work.WorkApi
import org.neo.gomina.module.GominaModule
import org.neo.gomina.plugins.PluginAssembler

class WebVerticle : AbstractVerticle() {

    @Throws(Exception::class)
    override fun start() {
        logger.info("Starting...")

        val injector = Guice.createInjector(Modules.combine(GominaModule(), object : AbstractModule() {
            override fun configure() {
                bind(Vertx::class.java).toInstance(vertx)
            }
        }))

        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create())

        injector.getInstance(PluginAssembler::class.java).init()

        router
                .mountSubRouter("/authenticate", injector.getInstance(AuthApi::class.java).router)
                .mountSubRouter("/data/user", injector.getInstance(UsersApi::class.java).router)
                .mountSubRouter("/data/envs", injector.getInstance(EnvsApi::class.java).router)
                .mountSubRouter("/data/projects", injector.getInstance(ProjectsApi::class.java).router)
                .mountSubRouter("/data/work", injector.getInstance(WorkApi::class.java).router)
                .mountSubRouter("/data/instances", injector.getInstance(InstancesApi::class.java).router)
                .mountSubRouter("/data/hosts", injector.getInstance(HostsApi::class.java).router)
                .mountSubRouter("/data/events", injector.getInstance(EventsApi::class.java).router)
                .mountSubRouter("/data/dependencies", injector.getInstance(DependenciesApi::class.java).router)
                .mountSubRouter("/data/diagram", injector.getInstance(DiagramApi::class.java).router)
                .mountSubRouter("/realtime", injector.getInstance(NotificationsApi::class.java).apply { start() }.router)

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
