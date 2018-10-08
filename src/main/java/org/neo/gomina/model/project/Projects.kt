package org.neo.gomina.model.project

data class ScmLocation (var svnRepo: String = "", var svnUrl: String = "")

data class Project (
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
    var jenkinsJob: String? = null
)

data class Scm (
    var type: String = "",
    var url: String = "",
    var path: String = "",
    val username: String = "",
    val passwordAlias: String = ""
) {
    val id: String get() = "$type-$url-$path"
    val fullUrl: String get() = "$url" + if (path.isNotBlank()) "/$path" else "$path"
}


interface Projects {
    fun getProjects(): List<Project>
    fun getProject(projectId: String): Project?
}



