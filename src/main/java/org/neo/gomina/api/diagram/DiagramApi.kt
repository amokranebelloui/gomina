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
data class DiagramDetail (val components: List<DiagramComponentDetail>, val dependencies: List<DiagramDependencyDetail>)

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

        router.post("/update").handler(this::updateDiagram)
        router.get("/data").handler(this::data)
    }

    fun data(ctx: RoutingContext) {
        try {
            logger.info("Get Diagram data")

            val diagram = diagrams.get("main")
            val names = diagram.components.map { it.name }

            val allInteractions = interactionsRepository.getAll().associateBy { p -> p.serviceId }
            val functions = Dependencies.functions(allInteractions.values)
            val dependencies = Dependencies.dependencies(functions)
                    //.filter { it.to == componentId }
                    .filter { names.contains(it.from) && names.contains(it.to) }
                    .map { DiagramDependencyDetail(it.from, it.to) }

            //val diagramDetail = diagramBuilder.getDiagram()
            val diagramDetail = DiagramDetail(
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

    private fun updateDiagram(ctx: RoutingContext) {
        try {
            val json = ctx.bodyAsJson
            logger.info("Update ... $json")
            val name = json.getString("name")
            val x = json.getInteger("x")
            val y = json.getInteger("y")
            diagrams.update("main", DiagramComponent(name, x, y))
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot update Diagram data", e)
            ctx.fail(500)
        }
    }
}