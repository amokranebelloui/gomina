package org.neo.gomina.model.service

data class Service (
        var id: String, var type: String?, var systems: List<String> = emptyList(), var projectId: String?) {
    fun belongsToOneOf(systems: List<String>) = systems.isEmpty() || systems.intersect(this.systems).isNotEmpty()
}

interface Services {
    fun getServices(): List<Service>
}