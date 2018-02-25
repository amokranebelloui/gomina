package org.neo.gomina.model.scm

import org.apache.commons.lang3.StringUtils
import org.neo.gomina.model.maven.MavenUtils
import java.util.*


data class Commit (
    var revision: String = "",
    var date: Date? = null,
    var author: String? = null,
    var message: String? = null,

    var release: String? = null, // new version: if the commit is a post release version change
    var newVersion: String? = null // version: if the commit is a release
)

interface ScmClient {

    /** Get log from HEAD to revision, max @count elements */
    @Throws(Exception::class)
    fun getLog(url: String, rev: String, count: Int): List<Commit>

    /** get file for a revision, HEAD is -1 **/
    @Throws(Exception::class)
    fun getFile(url: String, rev: String): String?

}

interface ReleaseFlagger {
    fun flag(commit: Commit): Commit
}

class NoOpReleaseFlagger : ReleaseFlagger {
    override fun flag(commit: Commit) = commit
}

class MavenReleaseFlagger(val scmClient: ScmClient, val svnUrl: String) : ReleaseFlagger {
    override fun flag(commit: Commit): Commit {
        if (StringUtils.startsWith(commit.message, "[maven-release-plugin] prepare release")) {
            val pom = scmClient.getFile(svnUrl + "/trunk/pom.xml", commit.revision)
            commit.release = MavenUtils.extractVersion(pom)
        }
        if (StringUtils.startsWith(commit.message, "[maven-release-plugin]")) {
            val pom = scmClient.getFile(svnUrl + "/trunk/pom.xml", commit.revision)
            commit.newVersion = MavenUtils.extractVersion(pom)
        }
        return commit // FIXME Make immutable
    }
}