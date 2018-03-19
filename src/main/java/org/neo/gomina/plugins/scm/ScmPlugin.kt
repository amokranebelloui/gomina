package org.neo.gomina.plugins.scm

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.instances.Instance
import org.neo.gomina.core.instances.Instances
import org.neo.gomina.core.instances.InstancesExt
import org.neo.gomina.core.projects.CommitLogEntry
import org.neo.gomina.core.projects.ProjectDetail
import org.neo.gomina.core.projects.ProjectSet
import org.neo.gomina.core.projects.ProjectsExt
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.maven.MavenUtils
import org.neo.gomina.model.project.Projects
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.scm.MavenReleaseFlagger
import org.neo.gomina.model.scm.ScmClient
import org.neo.gomina.model.scm.ScmRepos
import org.neo.gomina.plugins.scm.ScmRetrieveStrategy.*
import java.io.File
import javax.inject.Inject

enum class ScmRetrieveStrategy { CACHE, SCM, SCM_DELTA, }

class ScmPlugin : InstancesExt, ProjectsExt {

    companion object {
        private val logger = LogManager.getLogger(ScmPlugin::class.java)
    }

    private val scmRepos: ScmRepos
    private val scmCache = ScmCache()

    @Inject private lateinit var inventory: Inventory
    @Inject private lateinit var projects: Projects

    @Inject
    constructor(scmRepos: ScmRepos) {
        this.scmRepos = scmRepos
        val file = File(".cache")
        if (!file.exists()) {
            val mkdir = file.mkdir()
            logger.info("Created $file $mkdir")
        }
    }

    override fun onGetProjects(projectSet: ProjectSet) {
        for (project in projects.getProjects()) {
            val projectDetail = projectSet.get(project.id)
            if (projectDetail != null) {
                val root = scmRepos.getRepo(project.svnRepo)?.location
                projectDetail.scmUrl = "$root${project.svnUrl}"
                val scmDetails = this.getSvnDetails(project.svnRepo, project.svnUrl)
                apply(projectDetail, scmDetails)
            }
        }
    }

    override fun onGetProject(projectId: String, projectDetail: ProjectDetail) {
        projects.getProject(projectId)
            ?. let {
                val commitLog = getCommits(it.svnRepo, it.svnUrl, scmRepos.getClient(it.svnRepo), retrieve = SCM_DELTA)
                val root = scmRepos.getRepo(it.svnRepo)?.location
                projectDetail.scmUrl = "$root${it.svnUrl}"
                Pair(getSvnDetails(it.svnRepo, it.svnUrl), commitLog)
            }
            ?. let { (svnDetail, commitLog) ->
                apply(projectDetail, svnDetail)
                projectDetail.commitLog = map(commitLog)
            }
    }

    private fun apply(projectDetail: ProjectDetail, scmDetails: ScmDetails) {
        projectDetail.changes = scmDetails.changes
        projectDetail.latest = scmDetails.latest
        projectDetail.released = scmDetails.released
    }

    override fun onReloadInstances(env: String) {
        projects.getProjects()
                .filter { StringUtils.isNotBlank(it.svnUrl) }
                .forEach { this.refresh(it.svnRepo, it.svnUrl) }
    }

    private fun map(commitLog: List<Commit>): List<CommitLogEntry> {
        return commitLog.map { CommitLogEntry(
                revision = it.revision,
                date = it.date,
                author = it.author,
                message = it.message
        ) }
    }

    override fun onGetInstances(env: String, instances: Instances) {
        for (env in inventory.getEnvironments()) {
            for (service in env.services) {
                for (envInstance in service.instances) {
                    val id = env.id + "-" + envInstance.id
                    val instance = instances.get(id)
                    val project = if (service.project != null) projects.getProject(service.project) else null
                    project?.let { instance?.applyScm(this.getSvnDetails(project.svnRepo, project.svnUrl)) }
                }
            }
        }
    }

    override fun onReloadProject(projectId: String) {
        val project = projects.getProject(projectId)
        this.refresh(project?.svnRepo ?: "", project?.svnUrl ?: "")
    }

    fun refresh(svnRepo: String, svnUrl: String) {
        if (svnUrl.isNotBlank()) {
            val scmClient = scmRepos.getClient(svnRepo)
            val logEntries = getCommits(svnRepo, svnUrl, scmClient, retrieve = SCM)
            val scmDetails = computeScmDetails(svnRepo, svnUrl, logEntries, scmClient)
            scmCache.cacheLog(svnRepo, svnUrl, logEntries)
            scmCache.cacheDetail(svnRepo, svnUrl, scmDetails)
        }
    }

    fun getSvnDetails(svnRepo: String, svnUrl: String): ScmDetails {
        if (svnUrl.isNotBlank()) {
            val detail = scmCache.getDetail(svnRepo, svnUrl)
            return if (detail != null) detail
            else {
                val scmClient = scmRepos.getClient(svnRepo)
                val logEntries = getCommits(svnRepo, svnUrl, scmClient, retrieve = CACHE)
                val scmDetails = computeScmDetails(svnRepo, svnUrl, logEntries, scmClient)
                scmCache.cacheDetail(svnRepo, svnUrl, scmDetails)
                scmCache.cacheLog(svnRepo, svnUrl, logEntries)
                logger.info("SCM Detail Served from SCM " + scmDetails)
                scmDetails
            }
        }
        else {
            return ScmDetails()
        }
    }

    private fun computeScmDetails(svnRepo: String, svnUrl: String, logEntries: List<Commit>, scmClient: ScmClient): ScmDetails {
        logger.info("Svn Details for " + svnUrl)
        return try {
            val lastReleasedRev = logEntries
                    .filter { StringUtils.isNotBlank(it.newVersion) }
                    .firstOrNull()?.revision

            val scmDetails = ScmDetails(
                    url = svnUrl,
                    latest = MavenUtils.extractVersion(scmClient.getFile(svnUrl + "/trunk/pom.xml", "-1")),
                    latestRevision = logEntries.firstOrNull()?.revision,
                    released = logEntries
                            .filter { StringUtils.isNotBlank(it.release) }
                            .firstOrNull()?.release,
                    releasedRevision = lastReleasedRev,
                    changes = commitCountTo(logEntries, lastReleasedRev)
            )
            logger.info(scmDetails)
            scmDetails
        }
        catch (e: Exception) {
            logger.error("Cannot get SVN information for " + svnUrl, e)
            ScmDetails()
        }
    }

    private fun commitCountTo(logEntries: List<Commit>, refRev: String?): Int? {
        var count = 0
        for ((revision) in logEntries) {
            if (StringUtils.equals(revision, refRev)) {
                return count
            }
            count++
        }
        return null
    }

    private fun getCommits(svnRepo: String, svnUrl: String, scmClient: ScmClient, retrieve: ScmRetrieveStrategy): List<Commit> {
        val mavenReleaseFlagger = MavenReleaseFlagger(scmClient, svnUrl)
        return when (retrieve) {
            CACHE -> {
                scmCache.getLog(svnRepo, svnUrl)
            }
            SCM -> {
                scmClient.getLog(svnUrl, "0", 100).map { mavenReleaseFlagger.flag(it) }
            }
            SCM_DELTA -> {
                val cached = scmCache.getLog(svnRepo, svnUrl)
                val lastKnown = cached.firstOrNull()?.revision ?: "0"
                val commits = scmClient.getLog(svnUrl, lastKnown, 100)
                        .map { mavenReleaseFlagger.flag(it) }
                        .filter { it.revision != lastKnown }
                logger.info("Get commits: cache=${cached.size} retrieved=${commits.size}")
                commits + cached
            }
        }
    }

}

fun Instance.applyScm(scmDetails: ScmDetails) {
    this.latestVersion = scmDetails.latest
    this.latestRevision = scmDetails.latestRevision
    this.releasedVersion = scmDetails.released
    this.releasedRevision = scmDetails.releasedRevision
}

