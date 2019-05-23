package org.neo.gomina.api.diagram

import com.google.inject.Inject
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.dependency.Dependencies
import org.neo.gomina.model.dependency.InteractionsRepository
import org.neo.gomina.model.diagram.DiagramComponent
import org.neo.gomina.model.diagram.Diagrams

data class DiagramComponentDetail (val name: String, val x: Int = 0, val y: Int = 0)
data class DiagramDependencyDetail (val from: String, val to: String)
data class DiagramDetail (
        val diagramId: String,
        val name: String,
        val description: String?,
        val components: List<DiagramComponentDetail>,
        val dependencies: List<DiagramDependencyDetail>)

class DiagramApi {

    companion object {
        private val logger = LogManager.getLogger(DiagramApi::class.java)
    }

    val router: Router

    @Inject lateinit var interactionsRepository: InteractionsRepository
    @Inject lateinit var diagrams: Diagrams

    @Inject
    constructor(vertx: Vertx) {
        this.router = Router.router(vertx)

        router.get("/all").handler(this::all)

        router.get("/data").handler(this::data)
        router.post("/node/update").handler(this::updateNode)
        router.delete("/node/remove").handler(this::removeNode)

        router.post("/add").handler(this::add)
        router.delete("/delete").handler(this::delete)
    }

    fun all(ctx: RoutingContext) {
        try {
            logger.info("Get Diagrams")
            val diagrams = diagrams.all()
            ctx.response().putHeader("content-type", "text/javascript").end(Json.encode(diagrams))
        } catch (e: Exception) {
            logger.error("Cannot get Diagrams", e)
            ctx.fail(500)
        }
    }

    fun data(ctx: RoutingContext) {
        try {
            val diagramId = ctx.request().getParam("diagramId")
            logger.info("Get Diagram data")

            val diagram = diagrams.get(diagramId)
            val names = diagram.components.map { it.name }

            val allInteractions = interactionsRepository.getAll().associateBy { p -> p.serviceId }
            val functions = Dependencies.functions(allInteractions.values)
            val dependencies = Dependencies.dependencies(functions)
                    //.filter { it.to == componentId }
                    .filter { names.contains(it.from) && names.contains(it.to) }
                    .map { DiagramDependencyDetail(it.from, it.to) }

            //val diagramDetail = diagramBuilder.getDiagram()
            val diagramDetail = DiagramDetail(
                    diagram.ref.diagramId, diagram.ref.name, diagram.ref.description,
                    diagram.components.map { DiagramComponentDetail(it.name, it.x, it.y) },
                    dependencies
            )

            logger.info(diagramDetail)

            ctx.response().putHeader("content-type", "text/javascript").end(Json.encode(diagramDetail))
        } catch (e: Exception) {
            logger.error("Cannot get Diagram data", e)
            ctx.fail(500)
        }
    }

    private fun updateNode(ctx: RoutingContext) {
        try {
            val diagramId = ctx.request().getParam("diagramId")
            val json = ctx.bodyAsJson
            logger.info("Update ... $json")
            val name = json.getString("name")
            val x = json.getInteger("x")
            val y = json.getInteger("y")
            diagrams.update(diagramId, DiagramComponent(name, x, y))
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot update Diagram data", e)
            ctx.fail(500)
        }
    }

    private fun removeNode(ctx: RoutingContext) {
        try {
            val diagramId = ctx.request().getParam("diagramId")
            val json = ctx.bodyAsJson
            logger.info("Remove ... $json")
            val name = json.getString("name")
            diagrams.remove(diagramId, name)
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot remove Diagram data", e)
            ctx.fail(500)
        }
    }

    private fun add(ctx: RoutingContext) {
        val diagramId = ctx.request().getParam("diagramId")
        try {
            val json = ctx.bodyAsJson
            logger.info("Add ... $diagramId $json")
            diagrams.add(diagramId, json.getString("name"), json.getString("description"))
            ctx.response().putHeader("content-type", "text/javascript").end(diagramId)
        }
        catch (e: Exception) {
            logger.error("Cannot add Diagram", e)
            ctx.fail(500)
        }
    }

    private fun delete(ctx: RoutingContext) {
        val diagramId = ctx.request().getParam("diagramId")
        try {
            logger.info("Delete ... $diagramId")
            val deleted = diagrams.delete(diagramId)
            if (deleted) {
                ctx.response().putHeader("content-type", "text/javascript").end(diagramId)
            }
            else {
                ctx.response().setStatusCode(400).end("Cannot delete non empty diagram")
            }
        }
        catch (e: Exception) {
            logger.error("Cannot delete Diagram", e)
            ctx.fail(500)
        }
    }
}