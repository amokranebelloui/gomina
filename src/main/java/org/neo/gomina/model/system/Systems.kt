package org.neo.gomina.model.system

import org.neo.gomina.model.component.ComponentRepo
import javax.inject.Inject

object System {
    fun extend(system: String): List<String> {
        var previous:String? = null
        return system.split(".").map {
            ((previous?.let { "$it." } ?: "") + it).also { previous = it }
        }
    }
    fun extend(systems: List<String>): List<String> {
        return systems.flatMap { extend(it) }
    }
}

interface Systems {
    fun getSystems(): List<String>
}

class InferredSystems : Systems {
    @Inject private lateinit var componentRepo: ComponentRepo
    override fun getSystems() =
        componentRepo.getAll()
            .flatMap { it.systems }
            .flatMap { System.extend(it) }
            .filter { it.isNotEmpty() }
            .toSet().toList()
}