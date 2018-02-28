package org.neo.gomina.api.diagram

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
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