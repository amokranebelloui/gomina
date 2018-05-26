package org.neo.gomina.integration.scm

data class ScmRepo(val id: String = "", val type: String = "", val location: String = "", val username: String = "", val passwordAlias: String = "")

interface ScmRepos {
    fun getRepo(id: String): ScmRepo?
    fun getClient(id: String): ScmClient
}

