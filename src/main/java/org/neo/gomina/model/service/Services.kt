package org.neo.gomina.model.service

import org.neo.gomina.model.project.System

data class Service (
        var id: String, var type: String?, var systems: List<String> = emptyList(), var projectId: String?) {
    fun belongsToOneOf(systems: List<String>) =
            systems.isEmpty() ||
            systems.intersect(this.systems.flatMap { System.extend(it) }).isNotEmpty()
}

interface Services {
    fun getServices(): List<Service>
}