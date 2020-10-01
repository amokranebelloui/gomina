package org.neo.gomina.integration.scm.impl

import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.scm.dummy.DummyScmClient
import org.neo.gomina.integration.scm.git.GitClient
import org.neo.gomina.integration.scm.none.NoneScmClient
import org.neo.gomina.integration.scm.svn.TmateSoftSvnClient
import org.neo.gomina.model.component.Scm
import org.neo.gomina.model.scm.ScmClient
import org.neo.gomina.model.security.Passwords
import java.util.*
import javax.inject.Inject

// FIXME Refactor

private val noOpScmClient = NoneScmClient()

class ScmClients {

    companion object {
        private val logger = LogManager.getLogger(ScmClients::class.java)
    }

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

    private fun buildScmClient(scm: Scm, passwords: Passwords): ScmClient? {
        return when (scm.type) {
            "svn" -> TmateSoftSvnClient(baseUrl = scm.url, projectUrl = scm.path,
                    username = scm.username, password = passwords.getRealPassword(scm.passwordAlias))
            "git", "git_local" -> GitClient(scm.url,
                    username = scm.username, password = passwords.getRealPassword(scm.passwordAlias),
                    local = scm.type == "git_local")
            "dummy" -> DummyScmClient(scm.url)
            else -> null
        }
    }

}
