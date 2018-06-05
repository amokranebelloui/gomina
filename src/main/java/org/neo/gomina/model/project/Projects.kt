package org.neo.gomina.model.project

data class ScmLocation (var svnRepo: String = "", var svnUrl: String = "")

data class Project (
    var id: String,
    var label: String? = null,
    var type: String? = null,
    var tags: String? = null,
    var svnRepo: String = "",
    var svnUrl: String = "",
    var maven: String? = null,
    var sonarServer: String = "",
    var jenkinsServer: String = "",
    var jenkinsJob: String? = null
)

interface Projects {
    fun getProjects(): List<Project>
    fun getProject(projectId: String): Project?
}



