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

data class CallChainDetail(val projectId: String, val recursive: Boolean, val functions: List<FunctionDetail> = emptyList(), val calls: List<CallChainDetail> = emptyList())

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
        router.get("/invocation/chain/:projectId").handler(this::getInvocationChain)
        router.get("/call/chain/:projectId").handler(this::getCallChain)
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
            val dependenciesDetails = dependencies.map { it.toDetail() }
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
                    //.filter { (f,stakeholders) -> stakeholders.users.find { it.projectId == projectId } != null }
            val dependencies = Dependencies.dependencies(functions).filter { it.from == projectId }.map { it.toDetail() }
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
                    //.filter { (f,stakeholders) -> stakeholders.exposers.contains(projectId) }
            val dependencies = Dependencies.dependencies(functions).filter { it.to == projectId }.map { it.toDetail() }
            ctx.response().putHeader("content-type", "text/javascript").end(Json.encode(dependencies))
        }
        catch (e: Exception) {
            logger.error("Cannot get Project Dependencies $projectId", e)
            ctx.fail(500)
        }
    }

    private fun getInvocationChain(ctx: RoutingContext) {
        val projectId = ctx.request().getParam("projectId")
        logger.info("Get Invocation Chain $projectId")
        try {
            val functions = Dependencies.functions(interactionsRepository.getAll())
            val dependencies = Dependencies.dependencies(functions)
            val invocationChain = Dependencies.invocationChain(projectId, dependencies).toDetail()
            ctx.response().putHeader("content-type", "text/javascript").end(Json.encode(invocationChain))
        }
        catch (e: Exception) {
            logger.error("Cannot get Invocation Chain $projectId", e)
            ctx.fail(500)
        }
    }

    private fun getCallChain(ctx: RoutingContext) {
        val projectId = ctx.request().getParam("projectId")
        logger.info("Get Call Chain $projectId")
        try {
            val functions = Dependencies.functions(interactionsRepository.getAll())
            val dependencies = Dependencies.dependencies(functions)
            val callChain = Dependencies.callChain(projectId, dependencies).toDetail()
            ctx.response().putHeader("content-type", "text/javascript").end(Json.encode(callChain))
        }
        catch (e: Exception) {
            logger.error("Cannot get Call Chain $projectId", e)
            ctx.fail(500)
        }
    }

    private fun FunctionUsage.toDetail() = FunctionDetail(
            name = this.function.name, type = this.function.type, usage = this.usage?.usage?.toString()
    )

    private fun Dependency.toDetail() = DependencyDetail(
            from = this.from,
            to = this.to,
            functions = this.functions.map { it.toDetail() }
    )

    private fun CallChain.toDetail(): CallChainDetail = CallChainDetail(
            projectId = this.projectId,
            recursive = this.recursive,
            functions = this.functions.map { it.toDetail() },
            calls = this.calls.map { it.toDetail() }
    )

}
