package org.neo.gomina.model.scm

import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.scm.dummy.DummyScmClient
import org.neo.gomina.model.scm.svn.TmateSoftSvnClient
import org.neo.gomina.model.security.Passwords
import java.util.*
import javax.inject.Inject

// Config
data class ScmConfig(val repos: List<ScmRepo> = ArrayList())
data class ScmRepo(val id: String = "", val type: String = "", val location: String = "", val username: String = "", val passwordAlias: String = "")

// Repo
interface ScmRepos {
    fun get(id: String): ScmClient
}

private class NoOpScmClient : ScmClient {
    override fun getLog(url: String, rev: String, count: Int): List<Commit> = emptyList()
    override fun getFile(url: String, rev: String): String? = null
}

private val noOpScmClient = NoOpScmClient()

class ConfigScmRepos : ScmRepos {

    companion object {
        private val logger = LogManager.getLogger(ConfigScmRepos::class.java)
    }
    
    private val clients = HashMap<String, ScmClient>()

    @Inject
    constructor(config: ScmConfig, passwords: Passwords) {
        for (repo in config.repos) {
            try {
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

    override fun get(id: String): ScmClient {
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

