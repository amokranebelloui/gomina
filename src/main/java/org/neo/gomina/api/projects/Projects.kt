package org.neo.gomina.api.projects

import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.project.Project
import org.neo.gomina.model.project.Projects
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.scminfo.ScmConnector
import org.neo.gomina.model.scminfo.ScmDetails
import org.neo.gomina.model.sonar.SonarConnector
import org.neo.gomina.model.sonar.SonarIndicators
import java.util.*
import javax.inject.Inject


data class ProjectDetail (
    val id: String?,
    val label: String?,
    val type: String?,
    val repo: String?,
    val svn: String?,
    val mvn: String?,
    val jenkins: String?,
    val changes: Int?,
    val latest: String?,
    val released: String?,
    val loc: Double?,
    val coverage: Double?,
    val commitLog: List<CommitLogEntry>
)

data class CommitLogEntry (
    val revision: String?,
    val date: Date?,
    val author: String?,
    val message: String?
)

class ProjectsBuilder {

    companion object {
        private val logger = LogManager.getLogger(ProjectsBuilder::class.java)
    }

    @Inject private lateinit var projects: Projects
    @Inject private lateinit var scmConnector: ScmConnector
    @Inject private lateinit var sonarConnector: SonarConnector

    fun getProjects(): List<ProjectDetail> {
        val result = ArrayList<ProjectDetail>()
        val sonarIndicatorsMap = sonarConnector.metrics
        for (project in projects.getProjects()) {
            val scmDetails = scmConnector.getSvnDetails(project.svnRepo, project.svnUrl)
            val sonarIndicators = sonarIndicatorsMap[project.maven]
            val projectDetail = build(project, scmDetails, emptyList(), sonarIndicators)
            result.add(projectDetail)
        }
        return result
    }

    fun getProject(projectId: String): ProjectDetail? {
        val project = projects.getProject(projectId)
        if (project != null) {
            val sonarIndicators = sonarConnector.getMetrics(project.maven)[project.maven]
            val commitLog: List<CommitLogEntry> = map(scmConnector.getCommitLog(project.svnRepo, project.svnUrl))
            val scmDetails: ScmDetails = scmConnector.getSvnDetails(project.svnRepo, project.svnUrl)
            return build(project, scmDetails, commitLog, sonarIndicators)
        }
        return null
    }

    private fun map(commitLog: List<Commit>): List<CommitLogEntry> {
        return commitLog.map { CommitLogEntry(
                revision = it.revision,
                date = it.date,
                author = it.author,
                message = it.message
        ) }
    }

    private fun build(project: Project, scmDetails: ScmDetails?, commitLog: List<CommitLogEntry>, sonarIndicators: SonarIndicators?): ProjectDetail {
        return ProjectDetail(
                id = project.id,
                label = project.label ?: project.id,
                type = project.type,
                repo = project.svnRepo,
                svn = project.svnUrl,
                mvn = project.maven,
                jenkins = project.jenkinsJob,

                changes = scmDetails?.changes,
                latest = scmDetails?.latest,
                released = scmDetails?.released,

                loc = sonarIndicators?.loc,
                coverage = sonarIndicators?.coverage,

                commitLog = commitLog
        )
    }

}