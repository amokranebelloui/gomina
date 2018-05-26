package org.neo.gomina.plugins.scm

import com.github.rjeschke.txtmark.Processor
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.instances.Instance
import org.neo.gomina.core.instances.InstanceDetailRepository
import org.neo.gomina.core.projects.CommitLogEntry
import org.neo.gomina.core.projects.ProjectDetail
import org.neo.gomina.core.projects.ProjectDetailRepository
import org.neo.gomina.integration.scm.ScmDetails
import org.neo.gomina.integration.scm.ScmRepos
import org.neo.gomina.integration.scm.cache.ScmCache
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.project.Projects
import org.neo.gomina.plugins.Plugin
import java.io.File
import javax.inject.Inject

private fun ProjectDetail.apply(scmDetails: ScmDetails) {
    this.scmUrl = scmDetails.url
    this.docFiles = scmDetails.docFiles
    this.changes = scmDetails.changes
    this.latest = scmDetails.latest
    this.released = scmDetails.released
    this.commitLog = scmDetails.commitLog.map {
        CommitLogEntry(
            revision = it.revision,
            date = it.date,
            author = it.author,
            message = it.message
        )
    }
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
            projectDetailRepository.getProject(project.id)
                    ?.apply(getSvnDetails(project.svnRepo, project.svnUrl))
        }

        for (env in inventory.getEnvironments()) {
            for (service in env.services) {
                for (envInstance in service.instances) {
                    val id = env.id + "-" + envInstance.id
                    val instance = instanceDetailRepository.getInstance(id)
                    service.project
                            ?.let { projects.getProject(it) }
                            ?.let { instance?.applyScm(this.getSvnDetails(it.svnRepo, it.svnUrl)) }
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
                    service.project
                            ?.let { projects.getProject(it) }
                            ?.let { instance?.applyScm(this.getSvnDetails(it.svnRepo, it.svnUrl)) }
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
            scmCache.cacheDetail(svnRepo, svnUrl, scmDetails)
            projectDetailRepository.getProject(projectId)
                    ?.apply(scmDetails)
        }
    }

    companion object {
        private val logger = LogManager.getLogger(ScmPlugin::class.java)
    }
}
