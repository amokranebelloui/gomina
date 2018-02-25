package org.neo.gomina.model.scminfo

import org.neo.gomina.model.scm.Commit

data class ScmDetails (
    var url: String? = null,
    var latest: String? = null,
    var latestRevision: String? = null,
    var released: String? = null,
    var releasedRevision: String? = null,
    var changes: Int? = null
)

interface ScmConnector {

    fun refresh(svnRepo: String, svnUrl: String)
    fun getSvnDetails(svnRepo: String, svnUrl: String): ScmDetails
    fun getCommitLog(svnRepo: String, svnUrl: String): List<Commit>

}