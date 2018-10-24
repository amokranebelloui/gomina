package org.neo.gomina.api.dependencies

import com.google.inject.Inject
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.dependency.*
import org.neo.gomina.model.component.ComponentRepo


data class FunctionDetail(val name: String, val type: String, val usage: String? = null)

data class DependencyDetail(var from: String, var to: String, var functions: List<FunctionDetail>)
data class DependencyService(val serviceId: String, val inScope: Boolean)
data class DependenciesDetail(val services: List<DependencyService>, val functionTypes: Set<String>, val dependencies: List<DependencyDetail>)

data class CallChainDetail(val serviceId: String, val recursive: Boolean, val functions: List<FunctionDetail> = emptyList(), val calls: List<CallChainDetail> = emptyList())

class DependenciesApi {

    companion object {
        private val logger = LogManager.getLogger(DependenciesApi::class.java)
    }

    val router: Router

    @Inject lateinit var componentRepo: ComponentRepo
    @Inject lateinit var interactionsRepository: InteractionsRepository

    @Inject
    constructor(vertx: Vertx) {
        this.router = Router.router(vertx)

        router.get("/").handler(this::get)
        router.get("/outgoing/:componentId").handler(this::getOutgoing)
        router.get("/incoming/:componentId").handler(this::getIncoming)
        router.get("/invocation/chain/:componentId").handler(this::getInvocationChain)
        router.get("/call/chain/:componentId").handler(this::getCallChain)
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

            val allServices =
                    componentRepo.getAll().map {
                        Thing(id = it.id, type = "unknown", systems = it.systems)
                    }
            val servicesInScope = allServices.filter { it.belongsToOneOf(systems) }.map { it.id }

            val allInteractions = interactionsRepository.getAll().associateBy { p -> p.serviceId }

            val selectedInteractions = allServices
                    //.filter { it.belongsToOneOf(systems) }
                    .map { allInteractions[it.id] ?: Interactions(serviceId = it.id) }
                    .map { it.filterFunctionTypes(functionTypes) }
            val functions = Dependencies.functions(selectedInteractions)
            val dependencies = Dependencies.dependencies(functions)
                    .filter { it.involves(servicesInScope) }


            val services = servicesInScope +
                    dependencies.flatMap { arrayListOf(it.from, it.to) }
                    //if (dependencies.any { it.from == "?" || it.to == "?" }) listOf("?") else emptyList()

            val g = TopologicalSort<Dependency>(services.toSet().toList()).also {
                dependencies.forEach { dependency -> it.addEdge(dependency.from, dependency.to, dependency) }
            }
            val dependenciesDetails = dependencies.map { it.toDetail() }
            val dependenciesDetail = DependenciesDetail(
                    services = g.sort().map { DependencyService(serviceId = it, inScope = servicesInScope.contains(it)) },
                    functionTypes = Dependencies.functions(allInteractions.values).map { (f, _) -> f.type }.toSet(),
                    dependencies = dependenciesDetails)
            ctx.response().putHeader("content-type", "text/javascript").end(Json.encode(dependenciesDetail))
        } catch (e: Exception) {
            logger.error("Cannot get Dependencies", e)
            ctx.fail(500)
        }
    }

    private fun getOutgoing(ctx: RoutingContext) {
        val componentId = ctx.request().getParam("componentId")
        logger.info("Get Dependencies $componentId")
        try {
            val allInteractions = interactionsRepository.getAll().associateBy { p -> p.serviceId }
            val functions = Dependencies.functions(allInteractions.values)
            val dependencies = Dependencies.dependencies(functions).filter { it.from == componentId }.map { it.toDetail() }
            ctx.response().putHeader("content-type", "text/javascript").end(Json.encode(dependencies))
        }
        catch (e: Exception) {
            logger.error("Cannot get Dependencies $componentId", e)
            ctx.fail(500)
        }
    }

    private fun getIncoming(ctx: RoutingContext) {
        val componentId = ctx.request().getParam("componentId")
        logger.info("Get Dependencies $componentId")
        try {
            val allInteractions = interactionsRepository.getAll().associateBy { p -> p.serviceId }
            val functions = Dependencies.functions(allInteractions.values)
            val dependencies = Dependencies.dependencies(functions).filter { it.to == componentId }.map { it.toDetail() }
            ctx.response().putHeader("content-type", "text/javascript").end(Json.encode(dependencies))
        }
        catch (e: Exception) {
            logger.error("Cannot get Dependencies $componentId", e)
            ctx.fail(500)
        }
    }

    private fun getInvocationChain(ctx: RoutingContext) {
        val componentId = ctx.request().getParam("componentId")
        logger.info("Get Invocation Chain $componentId")
        try {
            val functions = Dependencies.functions(interactionsRepository.getAll())
            val dependencies = Dependencies.dependencies(functions)
            val invocationChain = Dependencies.invocationChain(componentId, dependencies).toDetail()
            ctx.response().putHeader("content-type", "text/javascript").end(Json.encode(invocationChain))
        }
        catch (e: Exception) {
            logger.error("Cannot get Invocation Chain $componentId", e)
            ctx.fail(500)
        }
    }

    private fun getCallChain(ctx: RoutingContext) {
        val componentId = ctx.request().getParam("componentId")
        logger.info("Get Call Chain $componentId")
        try {
            val functions = Dependencies.functions(interactionsRepository.getAll())
            val dependencies = Dependencies.dependencies(functions)
            val callChain = Dependencies.callChain(componentId, dependencies).toDetail()
            ctx.response().putHeader("content-type", "text/javascript").end(Json.encode(callChain))
        }
        catch (e: Exception) {
            logger.error("Cannot get Call Chain $componentId", e)
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
            serviceId = this.serviceId,
            recursive = this.recursive,
            functions = this.functions.map { it.toDetail() },
            calls = this.calls.map { it.toDetail() }
    )

}
