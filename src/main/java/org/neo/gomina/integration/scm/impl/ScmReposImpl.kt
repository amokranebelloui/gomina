package org.neo.gomina.integration.scm.impl

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.maven.MavenUtils
import org.neo.gomina.integration.scm.Commit
import org.neo.gomina.integration.scm.ScmClient
import org.neo.gomina.integration.scm.ScmDetails
import org.neo.gomina.integration.scm.ScmRepos
import org.neo.gomina.integration.scm.dummy.DummyScmClient
import org.neo.gomina.integration.scm.git.GitClient
import org.neo.gomina.integration.scm.metadata.ProjectMetadataMapper
import org.neo.gomina.integration.scm.none.NoneScmClient
import org.neo.gomina.integration.scm.svn.TmateSoftSvnClient
import org.neo.gomina.integration.scm.versions.MavenReleaseFlagger
import org.neo.gomina.model.project.Scm
import org.neo.gomina.model.security.Passwords
import java.util.*
import javax.inject.Inject

// FIXME Refactor

private val noOpScmClient = NoneScmClient()

class ScmReposImpl : ScmRepos {

    companion object {
        private val logger = LogManager.getLogger(ScmReposImpl::class.java)
    }

    private val clients = HashMap<String, ScmClient>()

    private val metadataMapper = ProjectMetadataMapper()

    private val passwords: Passwords

    @Inject
    constructor(passwords: Passwords) {
        this.passwords = passwords
    }

    private fun getClient(scm: Scm): ScmClient {
        return clients.getOrPut("${scm.id}") {
            buildScmClient(scm, passwords) ?: noOpScmClient
        }
    }

    override fun getScmDetails(scm: Scm): ScmDetails {
        val scmClient = this.getClient(scm)
        val mavenReleaseFlagger = MavenReleaseFlagger(scmClient) // FIXME Detect build system
        val trunk = scmClient.getTrunk()
        val log = scmClient.getLog(trunk, "0", 100).map { mavenReleaseFlagger.flag(it) }
        return computeScmDetails(scm, log, scmClient)
    }

    private fun computeScmDetails(scm: Scm, logEntries: List<Commit>, scmClient: ScmClient): ScmDetails {
        logger.info("Svn Details for " + scm)
        return try {
            val lastReleasedRev = logEntries
                    .filter { StringUtils.isNotBlank(it.newVersion) }
                    .firstOrNull()?.revision
            
            // FIXME there shouldn't be trunk in here
            val metadataFile = scmClient.getFile("/trunk/project.yaml", "-1")
            val metadata = metadataFile?.let { metadataMapper.map(metadataFile) }

            val pomFile = scmClient.getFile("/trunk/pom.xml", "-1")

            val scmDetails = ScmDetails(
                    owner = metadata?.owner,
                    critical = metadata?.critical,
                    url = scm.url,
                    mavenId = MavenUtils.extractMavenId(pomFile),
                    latest = MavenUtils.extractVersion(pomFile),
                    latestRevision = logEntries.firstOrNull()?.revision,
                    released = logEntries
                            .filter { StringUtils.isNotBlank(it.release) }
                            .firstOrNull()?.release,
                    releasedRevision = lastReleasedRev,
                    branches = scmClient.getBranches(),
                    docFiles = scmClient.listFiles("/trunk/", "-1").filter { it.endsWith(".md") },
                    commitLog = logEntries,
                    changes = commitCountTo(logEntries, lastReleasedRev)
            )
            logger.info(scmDetails)
            scmDetails
        }
        catch (e: Exception) {
            logger.error("Cannot get SCM information for " + scm, e)
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

    override fun getBranch(scm: Scm, branchId: String): List<Commit> {
        val scmClient = this.getClient(scm)
        return scmClient.getLog(branchId, "0", -1)
    }

    override fun getDocument(scm: Scm, docId: String): String? {
        val scmClient = this.getClient(scm)
        // FIXME no TRUNK
        return scmClient.getFile("/trunk/$docId", "-1")
    }

    private fun buildScmClient(scm: Scm, passwords: Passwords): ScmClient? {
        return when (scm.type) {
            "svn" -> TmateSoftSvnClient(baseUrl = scm.url, projectUrl = scm.path, username = scm.username, password = passwords.getRealPassword(scm.passwordAlias))
            "git" -> GitClient(scm.url)
            "dummy" -> DummyScmClient(scm.url)
            else -> null
        }
    }

    // FIXME Reuse Incremental SVN loading
    /*
    enum class ScmRetrieveStrategy { CACHE, SCM, SCM_DELTA, }

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
    */

}
