package org.neo.gomina.model.scm

// Config
data class ScmRepo(val id: String = "", val type: String = "", val location: String = "", val username: String = "", val passwordAlias: String = "")

// Repo
interface ScmRepos {
    fun getRepo(id: String): ScmRepo?
    fun getClient(id: String): ScmClient
}

