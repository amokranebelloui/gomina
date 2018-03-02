package org.neo.gomina.model.scm

import java.util.*

// Config
data class ScmConfig(val repos: List<ScmRepo> = ArrayList())
data class ScmRepo(val id: String = "", val type: String = "", val location: String = "", val username: String = "", val passwordAlias: String = "")

// Repo
interface ScmRepos {
    fun get(id: String): ScmClient
}

