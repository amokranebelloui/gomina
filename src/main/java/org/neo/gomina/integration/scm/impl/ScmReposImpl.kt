package org.neo.gomina.integration.scm.impl

import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.scm.dummy.DummyScmClient
import org.neo.gomina.integration.scm.git.GitClient
import org.neo.gomina.integration.scm.none.NoneScmClient
import org.neo.gomina.integration.scm.svn.TmateSoftSvnClient
import org.neo.gomina.model.component.Scm
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.scm.ScmClient
import org.neo.gomina.model.security.Passwords
import java.util.*
import javax.inject.Inject

// FIXME Refactor

private val noOpScmClient = NoneScmClient()

class ScmReposImpl {

    companion object {
        private val logger = LogManager.getLogger(ScmReposImpl::class.java)
    }

    @Inject lateinit var commitDecorator: CommitDecorator

    private val clients = HashMap<String, ScmClient>()

    private val passwords: Passwords

    @Inject
    constructor(passwords: Passwords) {
        this.passwords = passwords
    }

    fun getClient(scm: Scm): ScmClient {
        return clients.getOrPut(scm.id) {
            buildScmClient(scm, passwords) ?: noOpScmClient
        }
    }

    // FIXME Number of commits to process ??

    fun getBranch(scm: Scm, branchId: String): List<Commit> {
        val scmClient = this.getClient(scm)
        return scmClient.getLog(branchId, "0", -1).map { commitDecorator.flag(it, scmClient) }
    }

    fun getLog(scmClient: ScmClient, trunk: String): List<Commit> {
        return scmClient.getLog(trunk, "0", 100).map { commitDecorator.flag(it, scmClient) }
    }

    fun getDocument(scm: Scm, docId: String): String? {
        val scmClient = this.getClient(scm)
        return scmClient.getFile(docId, "-1")
    }

    private fun buildScmClient(scm: Scm, passwords: Passwords): ScmClient? {
        return when (scm.type) {
            "svn" -> TmateSoftSvnClient(baseUrl = scm.url, projectUrl = scm.path, username = scm.username, password = passwords.getRealPassword(scm.passwordAlias))
            "git" -> GitClient(scm.url)
            "dummy" -> DummyScmClient(scm.url)
            else -> null
        }
    }

}
