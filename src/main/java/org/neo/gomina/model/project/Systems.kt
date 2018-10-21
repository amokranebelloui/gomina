package org.neo.gomina.model.project

import javax.inject.Inject

object System {
    fun extend(system: String): List<String> {
        var previous:String? = null
        return system.split(".").map {
            ((previous?.let { "$it." } ?: "") + it).also { previous = it }
        }
    }
    fun extend(systems: List<String>): List<String> {
        return systems.flatMap { System.extend(it) }
    }
}

interface Systems {
    fun getSystems(): List<String>
}

class ProjectSystems : Systems {
    @Inject private lateinit var projects: Projects
    override fun getSystems() =
        projects.getProjects()
            .flatMap { it.systems }
            .flatMap { System.extend(it) }
            .filter { it.isNotEmpty() }
            .toSet().toList()
}