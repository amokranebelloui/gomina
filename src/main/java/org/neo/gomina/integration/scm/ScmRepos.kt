package org.neo.gomina.integration.scm

import org.neo.gomina.integration.scm.impl.ScmRepo

// FIXME encapsulate document, and include type matadata

data class ScmDetails (
        var owner: String? = null,
        var critical: Int? = null,
        var url: String? = null,
        var mavenId: String? = null,
        var latest: String? = null,
        var latestRevision: String? = null,
        var released: String? = null,
        var releasedRevision: String? = null,
        var branches: List<Branch> = emptyList(),
        var docFiles: List<String> = emptyList(),
        var commitLog: List<Commit> = emptyList(),
        var changes: Int? = null
)

interface ScmRepos {
    operator fun get(id: String): ScmRepo?
    fun getDocument(id: String, svnUrl: String, docId: String): String?
    fun getScmDetails(id: String, svnUrl: String): ScmDetails
    fun getBranch(id: String, svnUrl: String, branchId: String): List<Commit>
}
