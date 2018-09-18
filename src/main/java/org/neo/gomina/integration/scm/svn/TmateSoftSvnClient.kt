package org.neo.gomina.integration.scm.svn

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.scm.Branch
import org.neo.gomina.integration.scm.Commit
import org.neo.gomina.integration.scm.ScmClient
import org.tmatesoft.svn.core.SVNDirEntry
import org.tmatesoft.svn.core.SVNLogEntry
import org.tmatesoft.svn.core.SVNProperties
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory
import org.tmatesoft.svn.core.io.SVNRepository
import org.tmatesoft.svn.core.io.SVNRepositoryFactory
import org.tmatesoft.svn.core.wc.SVNWCUtil
import java.io.ByteArrayOutputStream
import java.util.*

class TmateSoftSvnClient : ScmClient {

    companion object {
        private val logger = LogManager.getLogger(TmateSoftSvnClient::class.java)
    }
    
    private val url: String
    internal val repository: SVNRepository

    constructor(url: String, username: String? = null, password: String? = null) {
        this.url = url
        DAVRepositoryFactory.setup()
        repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url))

        if (StringUtils.isNotBlank(username)) {
            logger.info("Connecting to $url, using $username/***${StringUtils.length(password)}")
            repository.authenticationManager = SVNWCUtil.createDefaultAuthenticationManager(username, password)
        }
    }

    constructor(url: String) : this(url, null, null)

    override fun getTrunk(url: String): String {
        return "trunk"
    }

    override fun getBranches(url: String): List<Branch> {
        logger.info("Retrieve Branches")
        val entries = arrayListOf<SVNDirEntry>()
        repository.getDir("$url/branches", -1, null, entries)
        val result = entries.map {
            //this.getLog("$url/", Branch("${it.name}"), "0", 100).forEach { println(" " + it) }

            val svnProperties = arrayOf<String>()
            val logEntries = ArrayList<SVNLogEntry>()
            repository.log(arrayOf("$url/branches/${it.name}"), -1, 0,
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
        return result
    }

    override fun getLog(url: String, branch: String, startRev: String, count: Int): List<Commit> {
        logger.info("Retrieve SVN log from '$url' from rev '$startRev' max:$count")
        val path = "$url/$branch"
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
            Commit(
                    revision = revAsString(it.revision) ?: "",
                    date = it.date,
                    author = it.author,
                    message = StringUtils.replaceChars(it.message, "\n", " ")
                    //extra = it.changedPaths.toString() + it.isNonInheritable + it.isSubtractiveMerge
            )
        }
    }

    override fun getFile(url: String, rev: String): String? {
        try {
            val baos = ByteArrayOutputStream()
            repository.getFile(url, java.lang.Long.valueOf(rev), SVNProperties(), baos)
            return String(baos.toByteArray())
        } catch (e: Exception) {
            logger.info("Cannot find file $url")
        }
        return null
    }

    override fun listFiles(url: String, rev: String): List<String> {
        val dirEntries = ArrayList<SVNDirEntry>()
        repository.getDir(url, rev.toLong(), SVNProperties()) { dirEntries.add(it) }
        return dirEntries.map { it.relativePath }
    }

    private fun revAsString(rev: Long?): String? {
        return rev?.toString()
    }

    override fun toString(): String {
        return "TmateSoftSvnClient{url='$url'}"
    }

}