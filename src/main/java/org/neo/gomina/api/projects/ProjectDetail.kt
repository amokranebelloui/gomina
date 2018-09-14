package org.neo.gomina.api.projects

import java.util.*

data class ProjectDetail (
        var id: String,
        var label: String? = null,
        var type: String? = null,
        var owner: String? = null,
        var critical: Int? = null,
        var systems: List<String> = emptyList(),
        var languages: List<String> = emptyList(),
        var tags: List<String> = emptyList(),
        var scmRepo: String? = null,
        var scmLocation: String? = null,
        var scmUrl: String? = null,
        var mvn: String? = null,
        var sonarUrl: String? = null,
        var jenkinsServer: String? = null,
        var jenkinsJob: String? = null,
        var jenkinsUrl: String? = null,
        var buildNumber: String? = null,
        var buildStatus: String? = null,
        var buildTimestamp: Long? = null,
        var docFiles: List<String> = emptyList(),
        var changes: Int? = null,
        var latest: String? = null,
        var released: String? = null,
        var loc: Double? = null,
        var coverage: Double? = null,
        var commitLog: List<CommitLogEntry> = emptyList(),
        var lastCommit: Date? = null,
        var commitActivity: Int? = null
)

data class CommitLogEntry (
        var revision: String?,
        var date: Date?,
        var author: String?,
        var message: String?,

        var version: String? = null
)

/*
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
*/