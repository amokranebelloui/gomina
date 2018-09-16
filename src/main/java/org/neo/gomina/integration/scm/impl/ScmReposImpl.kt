package org.neo.gomina.integration.scm.impl

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.maven.MavenUtils
import org.neo.gomina.integration.scm.Commit
import org.neo.gomina.integration.scm.ScmClient
import org.neo.gomina.integration.scm.ScmDetails
import org.neo.gomina.integration.scm.ScmRepos
import org.neo.gomina.integration.scm.dummy.DummyScmClient
import org.neo.gomina.integration.scm.metadata.ProjectMetadataMapper
import org.neo.gomina.integration.scm.none.NoneScmClient
import org.neo.gomina.integration.scm.svn.TmateSoftSvnClient
import org.neo.gomina.integration.scm.versions.MavenReleaseFlagger
import org.neo.gomina.model.security.Passwords
import java.util.*
import javax.inject.Inject

// FIXME Refactor

private val noOpScmClient = NoneScmClient()

data class ScmRepo(val id: String = "", val type: String = "", val location: String = "", val username: String = "", val passwordAlias: String = "")

data class ScmConfig(val repos: List<ScmRepo> = ArrayList())

class ScmReposImpl : ScmRepos {

    companion object {
        private val logger = LogManager.getLogger(ScmReposImpl::class.java)
    }

    private val repos = HashMap<String, ScmRepo>()
    private val clients = HashMap<String, ScmClient>()

    private val metadataMapper = ProjectMetadataMapper()

    @Inject
    constructor(config: ScmConfig, passwords: Passwords) {
        for (repo in config.repos) {
            try {
                repos.put(repo.id, repo)
                val client = buildScmClient(repo, passwords)
                if (client != null) {
                    clients.put(repo.id, client)
                }
                logger.info("Added ${repo.id} $client")
            } catch (e: Exception) {
                logger.info("Cannot build SCM client for " + repo.id, e)
            }
        }
    }

    override fun get(id: String): ScmRepo? = repos[id]

    private fun getRepo(id: String): ScmRepo? {
        return repos[id]
    }

    private fun getClient(id: String): ScmClient {
        return clients[id] ?: noOpScmClient
    }

    override fun getScmDetails(id: String, svnUrl: String): ScmDetails {
        val scmClient = this.getClient(id)
        val mavenReleaseFlagger = MavenReleaseFlagger(scmClient, svnUrl) // FIXME Detect build system
        val log = scmClient.getLog(svnUrl, "0", 100).map { mavenReleaseFlagger.flag(it) }
        return computeScmDetails(id, svnUrl, log, scmClient)
    }

    private fun computeScmDetails(id: String, svnUrl: String, logEntries: List<Commit>, scmClient: ScmClient): ScmDetails {
        logger.info("Svn Details for " + svnUrl)
        return try {
            val lastReleasedRev = logEntries
                    .filter { StringUtils.isNotBlank(it.newVersion) }
                    .firstOrNull()?.revision

            val repo = this.getRepo(id)
            val root = repo?.location
            val url = if (root != null && svnUrl != null) "$root$svnUrl" else null

            val metadataFile = scmClient.getFile("$svnUrl/trunk/project.yaml", "-1")
            val metadata = metadataFile?.let { metadataMapper.map(metadataFile) }

            val pomFile = scmClient.getFile("$svnUrl/trunk/pom.xml", "-1")

            val scmDetails = ScmDetails(
                    owner = metadata?.owner,
                    critical = metadata?.critical,
                    url = url,
                    mavenId = MavenUtils.extractMavenId(pomFile),
                    latest = MavenUtils.extractVersion(pomFile),
                    latestRevision = logEntries.firstOrNull()?.revision,
                    released = logEntries
                            .filter { StringUtils.isNotBlank(it.release) }
                            .firstOrNull()?.release,
                    releasedRevision = lastReleasedRev,
                    docFiles = scmClient.listFiles("$svnUrl/trunk/", "-1").filter { it.endsWith(".md") },
                    commitLog = logEntries,
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

    override fun getDocument(id: String, svnUrl: String, docId: String): String? {
        val scmClient = this.getClient(id)
        return scmClient.getFile("$svnUrl/trunk/$docId", "-1")
    }

    private fun buildScmClient(repo: ScmRepo, passwords: Passwords): ScmClient? {
        return when (repo.type) {
            "svn" -> TmateSoftSvnClient(repo.location, repo.username, passwords.getRealPassword(repo.passwordAlias))
            "dummy" -> DummyScmClient()
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
