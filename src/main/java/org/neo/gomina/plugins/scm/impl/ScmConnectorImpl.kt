package org.neo.gomina.plugins.scm.impl

import com.thoughtworks.xstream.XStream
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.maven.MavenUtils
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.scm.MavenReleaseFlagger
import org.neo.gomina.model.scm.ScmClient
import org.neo.gomina.model.scm.ScmRepos
import org.neo.gomina.plugins.scm.ScmConnector
import org.neo.gomina.plugins.scm.ScmDetails
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject

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

private class ScmCache {

    companion object {
        private val logger = LogManager.getLogger(ScmCache::class.java)
    }

    private val xStream = XStream()
    private val cache = HashMap<String, ScmDetails>()

    fun getDetail(svnRepo: String, svnUrl: String): ScmDetails? {
        val detailCacheFileName = svnRepo + "-" + svnUrl.replace("/".toRegex(), "-").replace("\\\\".toRegex(), "-")
        val detailCacheFile = File(".cache/" + detailCacheFileName)

        if (cache.containsKey(svnUrl)) {
            val scmDetails = cache[svnUrl]
            logger.info("SCM Detail Served from Memory Cache $scmDetails")
            return scmDetails
        }
        else if (detailCacheFile.exists()) {
            try {
                val scmDetails = xStream.fromXML(detailCacheFile) as ScmDetails
                cache.put(svnUrl, scmDetails)
                logger.info("SCM Detail Served from File Cache $scmDetails")
                return scmDetails
            }
            catch (e: Exception) {
                logger.debug("Error loading Cache", e)
                logger.info("Error loading Cache from File $detailCacheFile")
            }
        }
        return null
    }

    fun cacheDetail(svnRepo: String, svnUrl: String, scmDetails: ScmDetails) {
        val detailCacheFileName = svnRepo + "-" + svnUrl.replace("/".toRegex(), "-").replace("\\\\".toRegex(), "-")
        val cacheFile = File(".cache/" + detailCacheFileName)

        cache.put(svnUrl, scmDetails)
        try {
            xStream.toXML(scmDetails, FileOutputStream(cacheFile))
        } catch (e: FileNotFoundException) {
            logger.error("Saving cache for $cacheFile", e)
        }
    }

    fun getLog(svnRepo: String, svnUrl: String): List<Commit> {
        val logCacheFileName = svnRepo + "-" + svnUrl.replace("/".toRegex(), "-").replace("\\\\".toRegex(), "-")
        val cacheFile = File(".cache/$logCacheFileName.log")
        return if (cacheFile.exists()) xStream.fromXML(cacheFile) as List<Commit> else emptyList()
    }

    fun cacheLog(svnRepo: String, svnUrl: String, result: List<Commit>) {
        val logCacheFileName = svnRepo + "-" + svnUrl.replace("/".toRegex(), "-").replace("\\\\".toRegex(), "-")
        val cacheFile = File(".cache/$logCacheFileName.log")

        try {
            xStream.toXML(result, FileOutputStream(cacheFile))
        } catch (e: Exception) {
            logger.error("Cannot save cache in $cacheFile")
        }
    }

}

class CachedScmConnector: ScmConnector {

    companion object {
        private val logger = LogManager.getLogger(CachedScmConnector::class.java)
    }

    private val scmRepos: ScmRepos
    private val scmCache = ScmCache()

    @Inject
    constructor(scmRepos: ScmRepos) {
        this.scmRepos = scmRepos
        val file = File(".cache")
        if (!file.exists()) {
            val mkdir = file.mkdir()
            logger.info("Created $file $mkdir")
        }
    }

    override fun refresh(svnRepo: String, svnUrl: String) {
        if (svnUrl.isNotBlank()) {
            val scmClient = scmRepos.get(svnRepo)
            val logEntries = getCommits(svnRepo, svnUrl, scmClient, useCache = false)
            val scmDetails = computeScmDetails(svnRepo, svnUrl, logEntries, scmClient)
            scmCache.cacheLog(svnRepo, svnUrl, logEntries)
            scmCache.cacheDetail(svnRepo, svnUrl, scmDetails)
        }
    }

    override fun getSvnDetails(svnRepo: String, svnUrl: String): ScmDetails {
        if (svnUrl.isNotBlank()) {
            val detail = scmCache.getDetail(svnRepo, svnUrl)
            return if (detail != null) detail
            else {
                val scmClient = scmRepos.get(svnRepo)
                val logEntries = getCommits(svnRepo, svnUrl, scmClient, useCache = true)
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

    private fun getCommits(svnRepo: String, svnUrl: String, scmClient: ScmClient, useCache: Boolean): List<Commit> {
        val mavenReleaseFlagger = MavenReleaseFlagger(scmClient, svnUrl)
        return if (useCache) {
            val cached = scmCache.getLog(svnRepo, svnUrl)
            val lastKnown = cached.firstOrNull()?.revision ?: "0"

            val commits = scmClient.getLog(svnUrl, lastKnown, 100)
                    .map { mavenReleaseFlagger.flag(it) }
                    .filter { it.revision != lastKnown }

            logger.info("Get commits: cache=${cached.size} retrieved=${commits.size}")
            commits + cached
        }
        else {
            scmClient.getLog(svnUrl, "0", 100).map { mavenReleaseFlagger.flag(it) }
        }
    }

    override fun getCommitLog(svnRepo: String, svnUrl: String): List<Commit> {
        logger.info("Commit Log Served from SCM")
        return try {
            if (svnUrl.isNotBlank()) scmRepos.get(svnRepo).getLog(svnUrl, "0", 100) else emptyList()
        }
        catch (e: Exception) {
            logger.error("Cannot get commit log: $svnRepo $svnUrl")
            ArrayList()
        }
    }

}