package org.neo.gomina.integration.scm.none

import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.scm.ScmClient

class NoneScmClient : ScmClient {
    override fun getLog(branch: String, rev: String, count: Int): List<Commit> = emptyList()
    override fun getFile(branch: String, url: String, rev: String): String? = null
    override fun listFiles(url: String, rev: String): List<String> = emptyList()

}