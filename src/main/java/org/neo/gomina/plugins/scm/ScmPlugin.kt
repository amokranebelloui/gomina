package org.neo.gomina.plugins.scm

import com.github.rjeschke.txtmark.Processor
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.core.instances.Instance
import org.neo.gomina.core.projects.CommitLogEntry
import org.neo.gomina.core.projects.ProjectDetail
import org.neo.gomina.integration.scm.ScmDetails
import org.neo.gomina.integration.scm.ScmRepos
import org.neo.gomina.model.inventory.Inventory
import org.neo.gomina.model.project.Project
import org.neo.gomina.model.project.Projects
import org.neo.gomina.plugins.Plugin
import org.neo.gomina.utils.Cache
import javax.inject.Inject

fun ProjectDetail.apply(scmDetails: ScmDetails) {
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
    private val scmCache = Cache<ScmDetails>("scm") {
        it.docFiles = it.docFiles ?: emptyList()
    }

    @Inject private lateinit var inventory: Inventory
    @Inject private lateinit var projects: Projects

    @Inject
    constructor(scmRepos: ScmRepos) {
        this.scmRepos = scmRepos
    }

    fun enrich(project: Project, detail: ProjectDetail) {
        val svnDetails = getSvnDetails(project.svnRepo, project.svnUrl)
        detail.apply(svnDetails)
    }

    override fun init() {
        logger.info("Initializing SCM Data ...")
        /*
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
        */
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
            val detail = scmCache.get("$svnRepo-$svnUrl")
            return if (detail != null) detail
            else {
                val scmDetails = scmRepos.getScmDetails(svnRepo, svnUrl)
                scmCache.cache("$svnRepo-$svnUrl", scmDetails)
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
        logger.info("Reload instances $env")
        projects.getProjects()
                .filter { StringUtils.isNotBlank(it.svnUrl) }
                .forEach {
                    if (it.svnUrl.isNotBlank()) {
                        val scmDetails = this.scmRepos.getScmDetails(it.svnRepo, it.svnUrl)
                        this.scmCache.cache("${it.svnRepo}-${it.svnUrl}", scmDetails)
                    }
                }
    }

    fun reloadProject(projectId: String) {
        logger.info("Reload project $projectId")
        val project = projects.getProject(projectId)
        val svnRepo = project?.svnRepo ?: ""
        val svnUrl = project?.svnUrl ?: ""
        if (svnUrl.isNotBlank()) {
            val scmDetails = this.scmRepos.getScmDetails(svnRepo, svnUrl)
            this.scmCache.cache("$svnRepo-$svnUrl", scmDetails)
        }
    }

    companion object {
        private val logger = LogManager.getLogger(ScmPlugin::class.java)
    }
}
