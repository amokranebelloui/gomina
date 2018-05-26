package org.neo.gomina.integration.scm.impl

import org.apache.logging.log4j.LogManager
import org.neo.gomina.integration.scm.ScmClient
import org.neo.gomina.integration.scm.ScmRepo
import org.neo.gomina.integration.scm.ScmRepos
import org.neo.gomina.integration.scm.dummy.DummyScmClient
import org.neo.gomina.integration.scm.none.NoneScmClient
import org.neo.gomina.integration.scm.svn.TmateSoftSvnClient
import org.neo.gomina.model.security.Passwords
import java.util.*
import javax.inject.Inject

// FIXME Refactor

private val noOpScmClient = NoneScmClient()

data class ScmConfig(val repos: List<ScmRepo> = ArrayList())

class ScmReposImpl : ScmRepos {

    companion object {
        private val logger = LogManager.getLogger(ScmReposImpl::class.java)
    }

    private val repos = HashMap<String, ScmRepo>()
    private val clients = HashMap<String, ScmClient>()

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

    override fun getRepo(id: String): ScmRepo? {
        return repos[id]
    }

    override fun getClient(id: String): ScmClient {
        return clients[id] ?: noOpScmClient
    }

    private fun buildScmClient(repo: ScmRepo, passwords: Passwords): ScmClient? {
        return when (repo.type) {
            "svn" -> TmateSoftSvnClient(repo.location, repo.username, passwords.getRealPassword(repo.passwordAlias))
            "dummy" -> DummyScmClient()
            else -> null
        }
    }

}
