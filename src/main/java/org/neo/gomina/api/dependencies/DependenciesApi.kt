package org.neo.gomina.api.dependencies

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.inject.Inject
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.api.common.splitParams
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.dependency.*
import org.neo.gomina.model.dependency.Function
import org.neo.gomina.model.work.WorkList


data class FunctionDetail(val name: String, val type: String, val usage: String? = null, var sources:List<String>)

data class DependencyDetail(var from: String, var to: String, var functions: List<FunctionDetail>)

data class DependencyService(val serviceId: String, val inScope: Boolean)

data class DependenciesDetail(val services: List<DependencyService>,
                              val functionTypes: Set<String>,
                              val dependencies: List<DependencyDetail>)

data class CallChainDetail(val serviceId: String,
                           val recursive: Boolean,
                           val functions: List<FunctionDetail> = emptyList(),
                           val calls: List<CallChainDetail> = emptyList())

data class FunctionData(val name: String, val type: String)
data class FunctionUsageData(val name: String, val type: String, val usage: String? = null)

class DependenciesApi {

    companion object {
        private val logger = LogManager.getLogger(DependenciesApi::class.java)
    }

    val router: Router

    private val mapper = ObjectMapper().registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)

    @Inject lateinit var workList: WorkList
    @Inject lateinit var componentRepo: ComponentRepo
    @Inject lateinit var interactionsRepository: InteractionsRepository
    @Inject lateinit var libraries: Libraries
    @Inject lateinit var interactionProviders: InteractionProviders

    @Inject
    constructor(vertx: Vertx) {
        this.router = Router.router(vertx)

        router.get("/").handler(this::get)
        router.get("/outgoing/:componentId").handler(this::getOutgoing)
        router.get("/incoming/:componentId").handler(this::getIncoming)
        router.get("/invocation/chain/:componentId").handler(this::getInvocationChain)
        router.get("/call/chain/:componentId").handler(this::getCallChain)

        router.post("/reload").handler(this::reload)

        router.get("/api/:componentId").handler(this::getApi)
        router.post("/api/:componentId/add").handler(this::addApi)
        router.delete("/api/:componentId/remove").handler(this::removeApi)

        router.get("/usage/:componentId").handler(this::getUsage)
        router.post("/usage/:componentId/add").handler(this::addUsage)
        router.delete("/usage/:componentId/remove").handler(this::removeUsage)

        router.get("/libraries/:componentId").handler(this::libraries)
    }

    fun get(ctx: RoutingContext) {
        try {
            val systems = ctx.request().getParam("systems").splitParams()
            val workIds = ctx.request().getParam("workIds").splitParams()
            val functionTypes = ctx.request().getParam("functionTypes").splitParams()
            logger.info("Get Dependencies systems=$systems works=$workIds functions=$functionTypes")

            val allServices =
                    componentRepo.getAll().map {
                        Thing(id = it.id, type = "unknown", systems = it.systems)
                    }
            val selectedWorks = workIds.mapNotNull { workList.get(it) }.flatMap { it.components }.toSet()
            val servicesInScope = allServices
                    .filter { it.belongsToOneOf(systems) && workIds.isEmpty() || selectedWorks.contains(it.id) }
                    .map { it.id }

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

    private fun reload(ctx: RoutingContext) {
        logger.info("Reload dependency sources")
        try {
            interactionProviders.providers.forEach { provider ->
                try {
                    logger.info("Reload '${provider.name()}' dependency source")
                    interactionsRepository.update(provider.name(), provider.getAll())
                }
                catch (e: Exception) {
                    logger.error("Error reloading '${provider.name()}' dependency source")
                }
            }
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot reload dependency sources", e)
            ctx.fail(500)
        }
    }

    private fun getApi(ctx: RoutingContext) {
        val componentId = ctx.request().getParam("componentId")
        logger.info("Get API $componentId")
        try {
            val api = interactionsRepository.getApi(componentId)
            ctx.response().putHeader("content-type", "text/javascript").end(Json.encode(api.map { it.toDetail() }))
        }
        catch (e: Exception) {
            logger.error("Cannot get API $componentId", e)
            ctx.fail(500)
        }
    }

    private fun addApi(ctx: RoutingContext) {
        val componentId = ctx.request().getParam("componentId")
        try {
            val data = mapper.readValue<FunctionData>(ctx.body.toString())
            logger.info("Adding API $componentId $data")
            interactionsRepository.addApi(componentId, Function(data.name, data.type))
            ctx.response().putHeader("content-type", "text/javascript").end(mapper.writeValueAsString(componentId))
        }
        catch (e: Exception) {
            logger.error("Cannot Add API $componentId", e)
            ctx.fail(500)
        }
    }

    private fun removeApi(ctx: RoutingContext) {
        val componentId = ctx.request().getParam("componentId")
        try {
            val data = mapper.readValue<FunctionData>(ctx.body.toString())
            logger.info("Removing API $componentId $data")
            interactionsRepository.removeApi(componentId, data.name)
            ctx.response().putHeader("content-type", "text/javascript").end(mapper.writeValueAsString(componentId))
        }
        catch (e: Exception) {
            logger.error("Cannot Remove API $componentId", e)
            ctx.fail(500)
        }
    }

    private fun getUsage(ctx: RoutingContext) {
        val componentId = ctx.request().getParam("componentId")
        logger.info("Get Usage $componentId")
        try {
            val api = interactionsRepository.getUsage(componentId)
            ctx.response().putHeader("content-type", "text/javascript").end(Json.encode(api.map { it.toDetail() }))
        }
        catch (e: Exception) {
            logger.error("Cannot get Usage $componentId", e)
            ctx.fail(500)
        }
    }

    private fun addUsage(ctx: RoutingContext) {
        val componentId = ctx.request().getParam("componentId")
        try {
            val data = mapper.readValue<FunctionUsageData>(ctx.body.toString())
            logger.info("Adding Usage $componentId $data")
            interactionsRepository.addUsage(componentId, FunctionUsage(data.name, data.type, data.usage?.let { Usage(it) }))
            ctx.response().putHeader("content-type", "text/javascript").end(mapper.writeValueAsString(componentId))
        }
        catch (e: Exception) {
            logger.error("Cannot Add Usage $componentId", e)
            ctx.fail(500)
        }
    }

    private fun removeUsage(ctx: RoutingContext) {
        val componentId = ctx.request().getParam("componentId")
        try {
            val data = mapper.readValue<FunctionUsageData>(ctx.body.toString())
            logger.info("Removing Usage $componentId $data")
            interactionsRepository.removeUsage(componentId, data.name)
            ctx.response().putHeader("content-type", "text/javascript").end(mapper.writeValueAsString(componentId))
        }
        catch (e: Exception) {
            logger.error("Cannot Remove Usage $componentId", e)
            ctx.fail(500)
        }
    }

    private fun libraries(ctx: RoutingContext) {
        val componentId = ctx.request().getParam("componentId")
        logger.info("Get Libraries $componentId")
        try {
            val deps = libraries.dependencies(componentId).map { it.toStr() }
            ctx.response().putHeader("content-type", "text/javascript").end(Json.encode(deps))
        }
        catch (e: Exception) {
            logger.error("Cannot get Libraries $componentId", e)
            ctx.fail(500)
        }
    }

    private fun Function.toDetail() = FunctionDetail(
            name = this.name,
            type = this.type,
            sources = this.sources
    )

    private fun FunctionUsage.toDetail() = FunctionDetail(
            name = this.function.name,
            type = this.function.type,
            usage = this.usage?.usage?.toString(),
            sources = this.sources
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
