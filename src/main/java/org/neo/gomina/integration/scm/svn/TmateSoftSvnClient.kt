package org.neo.gomina.integration.scm.svn

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.scm.Commit
import org.neo.gomina.integration.scm.ScmClient
import org.tmatesoft.svn.core.*
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
    private val repository: SVNRepository

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

    override fun getLog(url: String, rev: String, count: Int): List<Commit> {
        logger.info("Retrieve SVN log from '$url' from rev '$rev' max:$count")
        val logEntries = ArrayList<SVNLogEntry>()
        repository.log(arrayOf(url + "/trunk"), -1, java.lang.Long.valueOf(rev), true, true, count.toLong(), ISVNLogEntryHandler { logEntries.add(it) })
        return logEntries.map {
            Commit(
                    revision = revAsString(it.revision) ?: "",
                    date = it.date,
                    author = it.author,
                    message = StringUtils.replaceChars(it.message, "\n", " ")
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