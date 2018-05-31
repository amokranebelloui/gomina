package org.neo.gomina.integration.scm.versions

import org.apache.commons.lang3.StringUtils
import org.neo.gomina.integration.maven.MavenUtils
import org.neo.gomina.integration.scm.Commit
import org.neo.gomina.integration.scm.ScmClient

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