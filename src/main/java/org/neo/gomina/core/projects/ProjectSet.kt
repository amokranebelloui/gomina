package org.neo.gomina.core.projects


import java.util.*

data class ProjectDetail (
        var id: String,
        var label: String? = null,
        var type: String? = null,
        var scmRepo: String? = null,
        var scmLocation: String? = null,
        var scmUrl: String? = null,
        var mvn: String? = null,
        var jenkinsServer: String? = null,
        var jenkinsJob: String? = null,
        var jenkinsUrl: String? = null,
        var changes: Int? = null,
        var latest: String? = null,
        var released: String? = null,
        var loc: Double? = null,
        var coverage: Double? = null,
        var commitLog: List<CommitLogEntry> = emptyList()
)

data class CommitLogEntry (
        var revision: String?,
        var date: Date?,
        var author: String?,
        var message: String?
)

class ProjectSet {
    val list = ArrayList<ProjectDetail>()
    private val index = HashMap<String, ProjectDetail>()

    fun get(id: String) = index[id]

    fun ensure(id: String): ProjectDetail {
        var project = index[id]
        if (project == null) {
            project = ProjectDetail(id = id)
            index.put(id, project)
            list.add(project)
        }
        return project
    }
}