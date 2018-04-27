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
import java.io.File
import java.io.IOException
import java.util.*

data class Component (val name: String, val x: Int = 0, val y: Int = 0)
data class Dependency (val from: String, val to: String)
data class Diagram (val components: List<Component>, val dependencies: List<Dependency>)

class DiagramBuilder {

    companion object {
        private val logger = LogManager.getLogger(DiagramBuilder::class.java)
    }

    private val components = HashMap<String, Component>()
    private val dependencies = ArrayList<Dependency>()

    private val objectMapper = ObjectMapper().registerKotlinModule()
            .configure(SerializationFeature.INDENT_OUTPUT, true)

    private val file = File("data/architecture.json")

    init {
        try {
            val diagram = objectMapper.readValue<Diagram>(file)
            diagram.components.forEach { components.put(it.name, it) }
            dependencies += diagram.dependencies
        }
        catch (e: IOException) {
            logger.error("Error reading diagram", e)
        }
    }

    fun updateComponent(name: String, x: Int, y: Int) {
        components[name] = Component(name, x, y)
        try {
            objectMapper.writeValue(file, getDiagram())
        }
        catch (e: IOException) {
            logger.error("Error writing diagram", e)
        }

    }

    fun getDiagram(): Diagram = Diagram(ArrayList(components.values), dependencies)
    
}

class DiagramApi {

    companion object {
        private val logger = LogManager.getLogger(DiagramApi::class.java)
        val diagramBuilder = DiagramBuilder()
    }

    val router: Router

    @Inject
    constructor(vertx: Vertx) {
        this.router = Router.router(vertx)

        router.post("/update").handler(this::updateDiagram)
        router.get("/data").handler(this::data)
    }

    fun data(ctx: RoutingContext) {
        try {
            logger.info("Get Diagram data")
            ctx.response().putHeader("content-type", "text/javascript").end(Json.encode(diagramBuilder.getDiagram()))
        } catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }
    }

    fun updateDiagram(ctx: RoutingContext) {
        try {
            val json = ctx.bodyAsJson
            logger.info("Update ... " + json)
            diagramBuilder.updateComponent(json.getString("name"), json.getInteger("x")!!, json.getInteger("y")!!)
            ctx.response().putHeader("content-type", "text/javascript").end()
        }
        catch (e: Exception) {
            logger.error("Cannot get instances", e)
            ctx.fail(500)
        }
    }
}