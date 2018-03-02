package org.neo.gomina.plugins.scm.connectors

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.scm.ScmClient
import org.tmatesoft.svn.core.ISVNLogEntryHandler
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
        val baos = ByteArrayOutputStream()
        repository.getFile(url, java.lang.Long.valueOf(rev), SVNProperties(), baos)
        return String(baos.toByteArray())
    }

    private fun revAsString(rev: Long?): String? {
        return rev?.toString()
    }

    override fun toString(): String {
        return "TmateSoftSvnClient{url='$url'}"
    }

}