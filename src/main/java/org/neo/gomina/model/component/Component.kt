package org.neo.gomina.model.component

import org.neo.gomina.model.project.System

data class Component(
        var id: String,
        var type: String?,
        var systems: List<String> = emptyList(),
        var projectId: String?) {
    fun belongsToOneOf(systems: List<String>) =
            systems.isEmpty() ||
            systems.intersect(this.systems.flatMap { System.extend(it) }).isNotEmpty()
}

interface Components {
    fun getComponents(): List<Component>
}