package org.neo.gomina.api.dependencies

import com.google.inject.Inject
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.dependency.*
import org.neo.gomina.model.project.Projects


data class FunctionDetail(val name: String, val type: String, val usage: String? = null)
data class DependencyDetail(var from: String, var to: String, var functions: List<FunctionDetail>)
data class DependenciesDetail(val projects: List<String>, val functionTypes: Set<String>, val dependencies: List<DependencyDetail>)

class DependenciesApi {

    companion object {
        private val logger = LogManager.getLogger(DependenciesApi::class.java)
    }

    val router: Router

    @Inject lateinit var projects: Projects
    @Inject lateinit var projectsDeps: ProjectsDeps


    @Inject
    constructor(vertx: Vertx) {
        this.router = Router.router(vertx)

        router.get("/").handler(this::get)
    }

    fun get(ctx: RoutingContext) {
        try {
            val systems = ctx.request().getParam("systems")
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?: emptyList()
            val functionTypes = ctx.request().getParam("functionTypes")
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?: emptyList()
            logger.info("Get Dependencies $systems $functionTypes")

            val allProjects = projects.getProjects()
            val selectedProjects = allProjects
                    .filter { systems.isEmpty() || systems.intersect(it.systems).isNotEmpty() }
                    .map { it.id }

            val projectsDepsMap = projectsDeps.getAll().associateBy { p -> p.projectId }

            val allProjectsDeps = selectedProjects
                    //.map { it.id }
                    .map { projectsDepsMap[it] ?: ProjectDeps(projectId = it) }

            val projectsDeps = allProjectsDeps.map {
                ProjectDeps(
                        projectId = it.projectId,
                        exposed = it.exposed.filter { functionTypes.isEmpty() || functionTypes.contains(it.type) },
                        used = it.used.filter { functionTypes.isEmpty() || functionTypes.contains(it.function.type) }
                )
            }
            val functions = Dependencies.functions(projectsDeps)
            val dependencies = Dependencies.dependencies(functions)
            // FIXME Usage with no exposing component
            val g = TopologicalSort<Dependency>(projectsDeps.map { it.projectId } + listOf("?")).also {
                dependencies.forEach { dependency -> it.addEdge(dependency.from, dependency.to, dependency) }
            }
            val dependenciesDetails = dependencies.map {
                DependencyDetail(
                        from = it.from,
                        to = it.to,
                        functions = it.functions.map {
                            FunctionDetail(name = it.function.name, type = it.function.type, usage = it.usage?.toString())
                        })
            }
            val dependenciesDetail = DependenciesDetail(
                    projects = g.sort(),
                    functionTypes = Dependencies.functions(allProjectsDeps).map { (f, _) -> f.type }.toSet(),
                    dependencies = dependenciesDetails)
            ctx.response().putHeader("content-type", "text/javascript").end(Json.encode(dependenciesDetail))
        } catch (e: Exception) {
            logger.error("Cannot get Dependencies", e)
            ctx.fail(500)
        }
    }

}
