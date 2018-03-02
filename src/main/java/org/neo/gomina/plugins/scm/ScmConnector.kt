package org.neo.gomina.plugins.scm

data class ScmDetails (
    var url: String? = null,
    var latest: String? = null,
    var latestRevision: String? = null,
    var released: String? = null,
    var releasedRevision: String? = null,
    var changes: Int? = null
)
