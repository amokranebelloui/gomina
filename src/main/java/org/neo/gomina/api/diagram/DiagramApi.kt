package org.neo.gomina.api.diagram

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
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
import java.io.File
import java.io.IOException
import java.util.*

data class DiagramComponentDetail (val name: String, val x: Int = 0, val y: Int = 0)
data class DiagramDependencyDetail (val from: String, val to: String)
data class DiagramDetail (val components: List<DiagramComponentDetail>, val dependencies: List<DiagramDependencyDetail>)

class DiagramBuilder {

    companion object {
        private val logger = LogManager.getLogger(DiagramBuilder::class.java)
    }

    private val components = HashMap<String, DiagramComponentDetail>()
    private val dependencies = ArrayList<DiagramDependencyDetail>()

    private val objectMapper = ObjectMapper().registerKotlinModule()
            .configure(SerializationFeature.INDENT_OUTPUT, true)

    private val file = File("data/architecture.json")

    init {
        try {
            val diagram = objectMapper.readValue<DiagramDetail>(file)
            diagram.components.forEach { components.put(it.name, it) }
            dependencies += diagram.dependencies
        }
        catch (e: IOException) {
            logger.error("Error reading diagram", e)
        }
    }

    fun updateComponent(name: String, x: Int, y: Int) {
        components[name] = DiagramComponentDetail(name, x, y)
        try {
            objectMapper.writeValue(file, getDiagram())
        }
        catch (e: IOException) {
            logger.error("Error writing diagram", e)
        }

    }

    fun getDiagram(): DiagramDetail = DiagramDetail(ArrayList(components.values), dependencies)
    
}

class DiagramApi {

    companion object {
        private val logger = LogManager.getLogger(DiagramApi::class.java)
        val diagramBuilder = DiagramBuilder()
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
            diagramBuilder.updateComponent(name, x, y)
            diagrams.update("main", DiagramComponent(name, x, y))
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot update Diagram data", e)
            ctx.fail(500)
        }
    }
}