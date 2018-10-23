package org.neo.gomina.api.dependencies

import org.neo.gomina.model.system.System

data class Thing(
        var id: String,
        var type: String?,
        var systems: List<String> = emptyList()) {
    fun belongsToOneOf(systems: List<String>) =
            systems.isEmpty() ||
            systems.intersect(System.extend(this.systems)).isNotEmpty()
}
