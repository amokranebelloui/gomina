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
        var docFiles: List<String> = emptyList(),
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

interface ProjectDetailRepository {
    fun getProjects(): Collection<ProjectDetail>
    fun getProject(projectId: String): ProjectDetail?
    fun addProject(projectDetail: ProjectDetail)
}

class ProjectDetailRepositoryImpl : ProjectDetailRepository {

    private val index = HashMap<String, ProjectDetail>()

    override fun getProjects(): Collection<ProjectDetail> {
        return index.values
    }

    override fun getProject(projectId: String): ProjectDetail? {
        return index[projectId]
    }

    override fun addProject(projectDetail: ProjectDetail) {
        index.put(projectDetail.id, projectDetail)
    }
}
