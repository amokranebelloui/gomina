package org.neo.gomina.model.project

data class ScmLocation (var svnRepo: String = "", var svnUrl: String = "")

data class Project (
    var id: String,
    var label: String?,
    var type: String?,
    var tags: String?,
    var svnRepo: String = "",
    var svnUrl: String = "",
    var maven: String?,
    var sonarServer: String = "",
    var jenkinsServer: String = "",
    var jenkinsJob: String?
)

interface Projects {
    fun getProjects(): List<Project>
    fun getProject(projectId: String): Project?
}



