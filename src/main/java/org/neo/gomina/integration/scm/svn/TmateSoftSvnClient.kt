package org.neo.gomina.integration.scm.svn

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.scm.impl.CommitDecorator
import org.neo.gomina.model.scm.Branch
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.scm.ScmClient
import org.tmatesoft.svn.core.SVNDirEntry
import org.tmatesoft.svn.core.SVNLogEntry
import org.tmatesoft.svn.core.SVNProperties
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory
import org.tmatesoft.svn.core.io.SVNRepository
import org.tmatesoft.svn.core.io.SVNRepositoryFactory
import org.tmatesoft.svn.core.wc.SVNWCUtil
import java.io.ByteArrayOutputStream
import java.time.ZoneOffset
import java.util.*

class TmateSoftSvnClient : ScmClient {

    companion object {
        private val logger = LogManager.getLogger(TmateSoftSvnClient::class.java)
    }
    
    private val baseUrl: String
    private val projectUrl: String
    internal val repository: SVNRepository

    private val commitDecorator = CommitDecorator()

    constructor(baseUrl: String, projectUrl: String, username: String? = null, password: String? = null) {
        this.baseUrl = baseUrl
        this.projectUrl = projectUrl
        DAVRepositoryFactory.setup()
        repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(baseUrl))

        if (StringUtils.isNotBlank(username)) {
            logger.info("Connecting to $baseUrl, using $username/***${StringUtils.length(password)}")
            repository.authenticationManager = SVNWCUtil.createDefaultAuthenticationManager(username, password)
        }
    }

    constructor(baseUrl: String, projectUrl: String) : this(baseUrl, projectUrl, null, null)

    override fun getTrunk(): String {
        return "trunk"
    }

    override fun getBranches(): List<Branch> {
        logger.info("Retrieve Branches")

        val entries = arrayListOf<SVNDirEntry>()
        try {
            repository.getDir("$projectUrl/branches", -1, null, entries)
        }
        catch (e: Exception) {
            logger.info("No branches for $projectUrl")
        }
        val result = entries.map {
            //this.getLog("$url/", Branch("${it.name}"), "0", 100).forEach { println(" " + it) }

            val svnProperties = arrayOf<String>()
            val logEntries = ArrayList<SVNLogEntry>()
            repository.log(arrayOf("$projectUrl/branches/${it.name}"), -1, 0,
                    true, // changedPath
                    true, // strictNode
                    -1,
                    false, // include merged
                    svnProperties,
                    { logEntries.add(it) })
            val copy = logEntries.lastOrNull()?.changedPaths?.values?.firstOrNull()
            Branch(name = "branches/${it.name}", origin = copy?.copyPath, originRevision = copy?.copyRevision?.toString())
        }
        logger.info("Retrieved ${result.size} Branches")
        return result + Branch("trunk")
    }

    override fun getLog(branch: String, startRev: String, count: Int): List<Commit> {
        logger.info("Retrieve SVN log from '$projectUrl' from rev '$startRev' max:$count")
        val path = "$projectUrl/$branch"
        logger.info("Path: $path")
        val svnProperties = arrayOf<String>()
        val logEntries = ArrayList<SVNLogEntry>()
        repository.log(arrayOf(path), -1, startRev.toLong(),
                true, // changedPath
                true, // strictNode
                count.toLong(),
                false, // include merged
                svnProperties,
                { logEntries.add(it) })
        //val info = repository.info(path, logEntries.last().revision)
        return logEntries.map {
            /*
            it.changedPaths.forEach { (k, p) ->
                println("-->" + k + " " +  p.kind + " " + p.type + " " + p.path + " " + p.copyPath + " " + p.copyRevision)
            }
            */
            val revision = revAsString(it.revision) ?: ""
            val message = StringUtils.replaceChars(it.message, "\n", " ")
            val version = commitDecorator.flag(revision, message, this)
            Commit(
                    revision = revision,
                    date = it.date.toInstant().atZone(ZoneOffset.UTC).toLocalDateTime(),
                    author = it.author,
                    message = message,
                    branches = listOf(branch),
                    version = version
                    //extra = it.changedPaths.toString() + it.isNonInheritable + it.isSubtractiveMerge
            )
        }
    }

    override fun getFile(path: String, rev: String): String? {
        val trunk = getTrunk()
        try {
            val baos = ByteArrayOutputStream()
            repository.getFile("$projectUrl/$trunk/$path", java.lang.Long.valueOf(rev), SVNProperties(), baos)
            return String(baos.toByteArray())
        } catch (e: Exception) {
            logger.info("Cannot find file $projectUrl/$trunk/$path")
        }
        return null
    }

    override fun listFiles(path: String, rev: String): List<String> {
        val trunk = getTrunk()
        val dirEntries = ArrayList<SVNDirEntry>()
        repository.getDir("$projectUrl/$trunk/$path", rev.toLong(), SVNProperties()) { dirEntries.add(it) }
        return dirEntries.map { it.relativePath }
    }

    private fun revAsString(rev: Long?): String? {
        return rev?.toString()
    }

    override fun toString(): String {
        return "TmateSoftSvnClient{url='$baseUrl/$projectUrl'}"
    }

}