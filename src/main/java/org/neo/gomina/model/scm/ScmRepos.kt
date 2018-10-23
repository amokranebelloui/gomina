package org.neo.gomina.model.scm

import org.neo.gomina.model.component.Scm

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
        var commitLog: List<Commit> = emptyList(), // FIXME Doesn't need to be here
        var changes: Int? = null
)

interface ScmRepos {
    fun getDocument(scm: Scm, docId: String): String?
    fun getScmDetails(scm: Scm): ScmDetails?
    fun getTrunk(scm: Scm): List<Commit>
    fun getBranch(scm: Scm, branchId: String): List<Commit>
}
