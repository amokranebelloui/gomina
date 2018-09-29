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
    @Inject lateinit var interactionsRepository: InteractionsRepository

    @Inject
    constructor(vertx: Vertx) {
        this.router = Router.router(vertx)

        router.get("/").handler(this::get)
        router.get("/outgoing/:projectId").handler(this::getOutgoing)
        router.get("/incoming/:projectId").handler(this::getIncoming)
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

            val allProjects = projects.getProjects().associateBy { it.id }

            val allInteractions = interactionsRepository.getAll().associateBy { p -> p.projectId }

            val selectedInteractions = allProjects.values
                    .filter { systems.isEmpty() || systems.intersect(it.systems).isNotEmpty() }
                    .map { it.id }
                    .map { allInteractions[it] ?: Interactions(projectId = it) }
                    .map { Interactions(
                                projectId = it.projectId,
                                exposed = it.exposed.filter { functionTypes.isEmpty() || functionTypes.contains(it.type) },
                                used = it.used.filter { functionTypes.isEmpty() || functionTypes.contains(it.function.type) }
                    )}
            val functions = Dependencies.functions(selectedInteractions)
            val dependencies = Dependencies.dependencies(functions)

            // FIXME Usage with no exposing component
            val g = TopologicalSort<Dependency>(selectedInteractions.map { it.projectId } + listOf("?")).also {
                dependencies.forEach { dependency -> it.addEdge(dependency.from, dependency.to, dependency) }
            }
            val dependenciesDetails = dependencies.map { it.map() }
            val dependenciesDetail = DependenciesDetail(
                    projects = g.sort(),
                    functionTypes = Dependencies.functions(allInteractions.values).map { (f, _) -> f.type }.toSet(),
                    dependencies = dependenciesDetails)
            ctx.response().putHeader("content-type", "text/javascript").end(Json.encode(dependenciesDetail))
        } catch (e: Exception) {
            logger.error("Cannot get Dependencies", e)
            ctx.fail(500)
        }
    }

    private fun getOutgoing(ctx: RoutingContext) {
        val projectId = ctx.request().getParam("projectId")
        logger.info("Get Project Dependencies $projectId")
        try {
            val allInteractions = interactionsRepository.getAll().associateBy { p -> p.projectId }
            val functions = Dependencies.functions(allInteractions.values)
                    .filter { (f,stakeholders) -> stakeholders.users.find { it.projectId == projectId } != null }
            val dependencies = Dependencies.dependencies(functions).filter { it.from == projectId }.map { it.map() }
            ctx.response().putHeader("content-type", "text/javascript").end(Json.encode(dependencies))
        }
        catch (e: Exception) {
            logger.error("Cannot get Project Dependencies $projectId", e)
            ctx.fail(500)
        }
    }

    private fun getIncoming(ctx: RoutingContext) {
        val projectId = ctx.request().getParam("projectId")
        logger.info("Get Project Dependencies $projectId")
        try {
            val allInteractions = interactionsRepository.getAll().associateBy { p -> p.projectId }
            val functions = Dependencies.functions(allInteractions.values)
                    .filter { (f,stakeholders) -> stakeholders.exposers.contains(projectId) }
            val dependencies = Dependencies.dependencies(functions).filter { it.to == projectId }.map { it.map() }
            ctx.response().putHeader("content-type", "text/javascript").end(Json.encode(dependencies))
        }
        catch (e: Exception) {
            logger.error("Cannot get Project Dependencies $projectId", e)
            ctx.fail(500)
        }
    }

    fun Dependency.map() = DependencyDetail(
            from = this.from,
            to = this.to,
            functions = this.functions.map {
                FunctionDetail(name = it.function.name, type = it.function.type, usage = it.usage?.toString())
            }
    )
}
