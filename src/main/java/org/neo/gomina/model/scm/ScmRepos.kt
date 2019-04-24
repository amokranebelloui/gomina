package org.neo.gomina.model.scm

// FIXME encapsulate document, and include type matadata

data class ScmDetails (
        var owner: String? = null,
        var critical: Int? = null,
        var url: String? = null,
        var artifactId: String? = null,
        var latest: String? = null,
        var latestRevision: String? = null,
        var released: String? = null,
        var releasedRevision: String? = null,
        var branches: List<Branch> = emptyList(),
        var docFiles: List<String> = emptyList(),
        var commitLog: List<Commit> = emptyList(), // FIXME Doesn't need to be here
        var changes: Int? = null
)

