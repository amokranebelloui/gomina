package org.neo.gomina.model.diagram

data class DiagramComponent(val name: String, val x: Int = 0, val y: Int = 0)
data class Diagram(val components: List<DiagramComponent>)

interface Diagrams {
    fun getAll(): List<String>
    fun get(diagramId: String): Diagram
    fun update(diagramId: String, component: DiagramComponent)
}