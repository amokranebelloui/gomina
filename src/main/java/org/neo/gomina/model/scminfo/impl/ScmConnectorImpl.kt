package org.neo.gomina.model.scminfo.impl

import com.thoughtworks.xstream.XStream
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.maven.MavenUtils
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.scm.MavenReleaseFlagger
import org.neo.gomina.model.scm.ScmClient
import org.neo.gomina.model.scm.ScmRepos
import org.neo.gomina.model.scminfo.ScmConnector
import org.neo.gomina.model.scminfo.ScmDetails
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject

open class DefaultScmConnector : ScmConnector {

    companion object {
        private val logger = LogManager.getLogger(DefaultScmConnector::class.java)
    }

    private val scmRepos: ScmRepos

    @Inject
    constructor(scmRepos: ScmRepos) {
        this.scmRepos = scmRepos
    }

    override fun refresh(svnRepo: String, svnUrl: String) {

    }

    override fun getSvnDetails(svnRepo: String, svnUrl: String): ScmDetails {
        logger.info("Svn Details for " + svnUrl)
        val scmClient = scmRepos.get(svnRepo)
        val scmDetails = ScmDetails()
        try {
            val pom = scmClient!!.getFile(svnUrl + "/trunk/pom.xml", "-1")
            val currentVersion = MavenUtils.extractVersion(pom)

            val logEntries = getCommits(svnRepo, svnUrl, scmClient)

            scmDetails.url = svnUrl
            scmDetails.latest = currentVersion

            val latestCommit = if (logEntries.size > 0) logEntries[0] else null
            val latestRevision = latestCommit?.revision
            scmDetails.latestRevision = latestRevision

            val lastReleasedVersion = logEntries
                    .filter { StringUtils.isNotBlank(it.release) }
                    .firstOrNull()?.release
            scmDetails.released = lastReleasedVersion

            //String lastReleasedRev = getLastReleaseRev(logEntries);
            val lastReleasedRev = logEntries
                    .filter { StringUtils.isNotBlank(it.newVersion) }
                    .firstOrNull()?.revision
            scmDetails.releasedRevision = lastReleasedRev

            scmDetails.changes = commitCountTo(logEntries, lastReleasedRev) //diff.size();
        } catch (e: Exception) {
            logger.error("Cannot get SVN information for " + svnUrl, e)
        }

        logger.info(scmDetails)
        return scmDetails
    }

    @Throws(Exception::class)
    open protected fun getCommits(svnRepo: String, svnUrl: String, scmClient: ScmClient): List<Commit> {
        val mavenReleaseFlagger = MavenReleaseFlagger(scmClient, svnUrl)
        return scmClient.getLog(svnUrl, "0", 100)
                .map( { mavenReleaseFlagger.flag(it) })
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

    override fun getCommitLog(svnRepo: String, svnUrl: String): List<Commit> {
        try {
            val scmClient = scmRepos.get(svnRepo)
            return scmClient!!.getLog(svnUrl, "0", 100)
        } catch (e: Exception) {
            logger.error("Cannot get commit log: $svnRepo $svnUrl")
            return ArrayList()
        }

    }
}


class CachedScmConnector: DefaultScmConnector, ScmConnector {

    companion object {
        private val logger = LogManager.getLogger(CachedScmConnector::class.java)
    }

    private val xStream = XStream()
    private val cache = HashMap<String, ScmDetails>()
    
    @com.google.inject.Inject
    constructor(scmRepos: ScmRepos) : super(scmRepos) {
        val file = File(".cache")
        if (!file.exists()) {
            val mkdir = file.mkdir()
            logger.info("Created $file $mkdir")
        }
    }

    private fun getDetailFileCache(svnRepo: String, svnUrl: String): File {
        val fileName = svnRepo + "-" + svnUrl.replace("/".toRegex(), "-").replace("\\\\".toRegex(), "-")
        return File(".cache/" + fileName)
    }

    private fun getLogCacheFile(svnRepo: String, svnUrl: String): File {
        val fileName = svnRepo + "-" + svnUrl.replace("/".toRegex(), "-").replace("\\\\".toRegex(), "-")
        return File(".cache/$fileName.log")
    }

    override fun refresh(svnRepo: String, svnUrl: String) {
        getFromScmAndCache(svnRepo, svnUrl)
    }

    override fun getSvnDetails(svnRepo: String, svnUrl: String): ScmDetails {
        val cacheFile = getDetailFileCache(svnRepo, svnUrl)
        var scmDetails: ScmDetails?
        if (cache.containsKey(svnUrl)) {
            scmDetails = cache[svnUrl]
            logger.info("SCM Detail Served from Memory Cache " + scmDetails!!)
        } else if (cacheFile.exists()) {
            try {
                scmDetails = xStream.fromXML(cacheFile) as ScmDetails
                cache.put(svnUrl, scmDetails)
                logger.info("SCM Detail Served from File Cache " + scmDetails)
            } catch (e: Exception) {
                logger.debug("Error loading cache", e)
                logger.info("Corrupted file, try to refresh...")
                scmDetails = getFromScmAndCache(svnRepo, svnUrl)
            }

        } else {
            scmDetails = getFromScmAndCache(svnRepo, svnUrl)
            logger.info("SCM Detail Served from SCM " + scmDetails)
        }
        return scmDetails ?: ScmDetails()
    }


    private fun getFromScmAndCache(svnRepo: String, svnUrl: String): ScmDetails {
        val cacheFile = getDetailFileCache(svnRepo, svnUrl)
        val scmDetails: ScmDetails
        scmDetails = super.getSvnDetails(svnRepo, svnUrl)
        cache.put(svnUrl, scmDetails)
        try {
            xStream.toXML(scmDetails, FileOutputStream(cacheFile))
        } catch (e: FileNotFoundException) {
            logger.error("Saving cache for $svnRepo $svnUrl", e)
        }

        return scmDetails
    }

    @Throws(Exception::class)
    override fun getCommits(svnRepo: String, svnUrl: String, scmClient: ScmClient): List<Commit> {
        val cacheFile = getLogCacheFile(svnRepo, svnUrl)
        val cached = if (cacheFile.exists()) xStream.fromXML(cacheFile) as List<Commit> else emptyList()
        val lastKnown = cached.firstOrNull()?.revision ?: "0"

        val mavenReleaseFlagger = MavenReleaseFlagger(scmClient, svnUrl)
        val commits = scmClient.getLog(svnUrl, lastKnown, 100)
                .map { mavenReleaseFlagger.flag(it) }
                .filter { it.revision != lastKnown }

        logger.info("Cached " + cached.size + " " + cached)
        logger.info("Retrieved " + commits.size + " " + commits)
        val result = commits + cached
        xStream.toXML(result, FileOutputStream(cacheFile))
        return result
    }

    override fun getCommitLog(svnRepo: String, svnUrl: String): List<Commit> {
        logger.info("Commit Log Served from SCM")
        return super.getCommitLog(svnRepo, svnUrl)
    }

}