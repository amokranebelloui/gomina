package org.neo.gomina.model.diagram

data class DiagramComponent(val name: String, val x: Int = 0, val y: Int = 0)
data class Diagram(val ref: DiagramRef, val components: List<DiagramComponent>)
data class DiagramRef(val diagramId: String, val name: String, val description: String?)

interface Diagrams {
    fun all(): List<DiagramRef>
    fun get(diagramId: String): Diagram
    fun update(diagramId: String, component: DiagramComponent)
    fun remove(diagramId: String, name: String)
    fun add(diagramId: String, name: String, description: String)
    fun delete(diagramId: String): Boolean
}