package org.neo.gomina.plugins.scm

import com.github.rjeschke.txtmark.Processor
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.instances.Instance
import org.neo.gomina.core.instances.InstanceDetailRepository
import org.neo.gomina.core.projects.CommitLogEntry
import org.neo.gomina.core.projects.ProjectDetail
import org.neo.gomina.core.projects.ProjectDetailRepository
import org.neo.gomina.integration.scm.Commit
import org.neo.gomina.integration.scm.ScmDetails
import org.neo.gomina.integration.scm.ScmRepos
import org.neo.gomina.integration.scm.cache.ScmCache
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.project.Projects
import org.neo.gomina.plugins.Plugin
import java.io.File
import javax.inject.Inject

private fun map(commitLog: List<Commit>): List<CommitLogEntry> {
    return commitLog.map { CommitLogEntry(
            revision = it.revision,
            date = it.date,
            author = it.author,
            message = it.message
    ) }
}
private fun apply(projectDetail: ProjectDetail, scmDetails: ScmDetails) {
    projectDetail.scmUrl = scmDetails.url
    projectDetail.docFiles = scmDetails.docFiles
    projectDetail.changes = scmDetails.changes
    projectDetail.latest = scmDetails.latest
    projectDetail.released = scmDetails.released
    projectDetail.commitLog = map(scmDetails.commitLog)
}


fun Instance.applyScm(scmDetails: ScmDetails) {
    this.latestVersion = scmDetails.latest
    this.latestRevision = scmDetails.latestRevision
    this.releasedVersion = scmDetails.released
    this.releasedRevision = scmDetails.releasedRevision
}

class ScmPlugin : Plugin {

    private val scmRepos: ScmRepos
    private val scmCache = ScmCache()

    @Inject private lateinit var inventory: Inventory
    @Inject private lateinit var projects: Projects

    @Inject lateinit var projectDetailRepository: ProjectDetailRepository
    @Inject lateinit var instanceDetailRepository: InstanceDetailRepository

    @Inject
    constructor(scmRepos: ScmRepos) {
        this.scmRepos = scmRepos
        val file = File(".cache")
        if (!file.exists()) {
            val mkdir = file.mkdir()
            logger.info("Created $file $mkdir")
        }
    }

    override fun init() {
        logger.info("Initializing SCM Data ...")
        for (project in projects.getProjects()) {
            val projectDetail = projectDetailRepository.getProject(project.id)
            if (projectDetail != null) {
                val scmDetails = this.getSvnDetails(project.svnRepo, project.svnUrl)
                apply(projectDetail, scmDetails)

            }
        }

        for (env in inventory.getEnvironments()) {
            for (service in env.services) {
                for (envInstance in service.instances) {
                    val id = env.id + "-" + envInstance.id
                    val instance = instanceDetailRepository.getInstance(id)
                    val project = if (service.project != null) projects.getProject(service.project) else null
                    project?.let { instance?.applyScm(this.getSvnDetails(project.svnRepo, project.svnUrl)) }
                }
            }
        }
        logger.info("SCM Data initialized")
    }

    fun getDocument(projectId: String, docId: String): String? {
        return projects.getProject(projectId) ?. let {
            val file = scmRepos.getDocument(it.svnRepo, it.svnUrl, docId)
            Processor.process(file)
        }
    }

    fun getSvnDetails(svnRepo: String, svnUrl: String): ScmDetails {
        if (svnUrl.isNotBlank()) {
            val detail = scmCache.getDetail(svnRepo, svnUrl)
            return if (detail != null) detail
            else {
                val scmDetails = scmRepos.getScmDetails(svnRepo, svnUrl)
                scmCache.cacheDetail(svnRepo, svnUrl, scmDetails)
                ///scmCache.cacheLog(svnRepo, svnUrl, logEntries)
                logger.info("SCM Detail Served from SCM " + scmDetails)
                scmDetails
            }
        }
        else {
            return ScmDetails()
        }
    }

    fun reloadInstances(env: String) {
        projects.getProjects()
                .filter { StringUtils.isNotBlank(it.svnUrl) }
                .forEach { this.refresh(it.id, it.svnRepo, it.svnUrl) }

        for (env in inventory.getEnvironments()) {
            for (service in env.services) {
                for (envInstance in service.instances) {
                    val id = env.id + "-" + envInstance.id
                    val instance = instanceDetailRepository.getInstance(id)
                    val project = if (service.project != null) projects.getProject(service.project) else null
                    project?.let { instance?.applyScm(this.getSvnDetails(project.svnRepo, project.svnUrl)) }
                }
            }
        }
    }

    fun reloadProject(projectId: String) {
        val project = projects.getProject(projectId)
        this.refresh(projectId, project?.svnRepo ?: "", project?.svnUrl ?: "")
    }

    fun refresh(projectId: String, svnRepo: String, svnUrl: String) {
        if (svnUrl.isNotBlank()) {
            val scmDetails = scmRepos.getScmDetails(svnRepo, svnUrl)
            ///scmCache.cacheLog(svnRepo, svnUrl, commitLog)
            scmCache.cacheDetail(svnRepo, svnUrl, scmDetails)

            val projectDetail = projectDetailRepository.getProject(projectId)
            projectDetail?.let { apply(projectDetail, scmDetails) }
        }
    }

    companion object {
        private val logger = LogManager.getLogger(ScmPlugin::class.java)
    }
}
