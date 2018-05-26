package org.neo.gomina.integration.scm

// FIXME encapsulate document, and include type matadata

data class ScmDetails (
        var url: String? = null,
        var latest: String? = null,
        var latestRevision: String? = null,
        var released: String? = null,
        var releasedRevision: String? = null,
        var docFiles: List<String> = emptyList(),
        var commitLog: List<Commit> = emptyList(),
        var changes: Int? = null
)

interface ScmRepos {
    fun getDocument(id: String, svnUrl: String, docId: String): String?
    fun getScmDetails(id: String, svnUrl: String): ScmDetails
}
