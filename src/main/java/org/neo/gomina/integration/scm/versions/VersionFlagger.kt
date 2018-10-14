package org.neo.gomina.integration.scm.versions

import org.apache.commons.lang3.StringUtils
import org.neo.gomina.integration.maven.MavenUtils
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.scm.ScmClient

interface ReleaseFlagger {
    fun flag(commit: Commit): Commit
}

class NoOpReleaseFlagger : ReleaseFlagger {
    override fun flag(commit: Commit) = commit
}

class MavenReleaseFlagger(private val scmClient: ScmClient) : ReleaseFlagger {
    override fun flag(commit: Commit): Commit {
        if (StringUtils.startsWith(commit.message, "[maven-release-plugin] prepare release")) {
            val pom = scmClient.getFile("/trunk/pom.xml", commit.revision)
            commit.release = MavenUtils.extractVersion(pom)
        }
        if (StringUtils.startsWith(commit.message, "[maven-release-plugin]")) {
            val pom = scmClient.getFile("/trunk/pom.xml", commit.revision)
            commit.newVersion = MavenUtils.extractVersion(pom)
        }
        return commit // FIXME Make immutable
    }
}
