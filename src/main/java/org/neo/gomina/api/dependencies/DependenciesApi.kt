package org.neo.gomina.api.dependencies

import com.google.inject.Inject
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.dependency.Dependencies
import org.neo.gomina.model.dependency.Dependency
import org.neo.gomina.model.dependency.ProjectsDeps
import org.neo.gomina.model.dependency.TopologicalSort


data class DependencyDetail(var from: String, var to: String, var functions: List<String>)
data class DependenciesDetail(val projects: List<String>, val dependencies: List<DependencyDetail>)

class DependenciesApi {

    companion object {
        private val logger = LogManager.getLogger(DependenciesApi::class.java)
    }

    val router: Router

    @Inject lateinit var projectsDeps: ProjectsDeps


    @Inject
    constructor(vertx: Vertx) {
        this.router = Router.router(vertx)

        router.get("/").handler(this::get)
    }

    fun get(ctx: RoutingContext) {
        try {
            logger.info("Get Dependencies")
            val projects = projectsDeps.getAll()
            val functions = Dependencies.functions(projects)
            val dependencies = Dependencies.dependencies(functions)
            // FIXME Usage with no exposing component
            val g = TopologicalSort<Dependency>(projects.map { it.projectId }).also {
                dependencies.forEach { dependency -> it.addEdge(dependency.from, dependency.to, dependency) }
            }
            val dependenciesDetails = dependencies.map {
                DependencyDetail(it.from, it.to, it.functions.map { "${it.function.name}|${it.function.type}|${it.usage ?: ""}" })
            }
            val dependenciesDetail = DependenciesDetail(projects = g.sort(), dependencies = dependenciesDetails)
            ctx.response().putHeader("content-type", "text/javascript").end(Json.encode(dependenciesDetail))
        } catch (e: Exception) {
            logger.error("Cannot get Dependencies", e)
            ctx.fail(500)
        }
    }

}
