package org.neo.gomina.model.component

import org.neo.gomina.model.system.System

data class Component(
        var id: String,
        var label: String? = null,
        var type: String? = null,
        var systems: List<String> = emptyList(),
        var languages: List<String> = emptyList(),
        var tags: List<String> = emptyList(),
        var scm: Scm? = null,
        var maven: String? = null,
        var sonarServer: String = "",
        var jenkinsServer: String = "",
        var jenkinsJob: String? = null) {
    fun shareSystem(other: Component): Boolean {
        return System.extend(this.systems).intersect(System.extend(other.systems)).isNotEmpty()
    }
}

data class Scm (
        var type: String = "",
        var url: String = "", var path: String = "",
        val username: String = "", val passwordAlias: String = "") {

    val id: String get() = "$type-$url-$path"
    val fullUrl: String get() = "$url" + if (path.isNotBlank()) "/$path" else "$path"
}


interface ComponentRepo {
    fun getAll(): List<Component>
    fun get(componentId: String): Component?
}



