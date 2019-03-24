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
        var jenkinsJob: String? = null,
        var disabled: Boolean
) {
    fun shareSystem(other: Component): Boolean {
        return System.extend(this.systems).intersect(System.extend(other.systems)).isNotEmpty()
    }
}

data class NewComponent(
        var id: String,
        var label: String? = null,
        var artifactId: String? = null,
        var type: String? = null,
        //var owner: String? = null,
        //var critical: Int? = null,
        var systems: List<String> = emptyList(),
        var languages: List<String> = emptyList(),
        var tags: List<String> = emptyList(),
        var scm: Scm? = null,
        var sonarServer: String? = null,
        var jenkinsServer: String? = null,
        var jenkinsJob: String? = null
)


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
    fun add(component: NewComponent)
    fun editLabel(componentId: String, label: String)
    fun editType(componentId: String, type: String)
    fun editArtifactId(componentId: String, artifactId: String?)
    fun editScm(componentId: String, type: String, url: String, path: String?)
    fun editSonar(componentId: String, server: String?)
    fun editBuild(componentId: String, server: String?, job: String?)
    fun addSystem(componentId: String, system: String)
    fun deleteSystem(componentId: String, system: String)
    fun addLanguage(componentId: String, language: String)
    fun deleteLanguage(componentId: String, language: String)
    fun addTag(componentId: String, tag: String)
    fun deleteTag(componentId: String, tag: String)
    fun disable(componentId: String)
    fun enable(componentId: String)
}



