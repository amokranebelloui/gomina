package org.neo.gomina.integration.scm.impl

import org.apache.commons.lang3.StringUtils
import org.neo.gomina.integration.maven.MavenUtils
import org.neo.gomina.model.scm.ScmClient

class CommitDecorator {

    fun flag(revision: String, message: String, scmClient: ScmClient): String? {
        // FIXME Detect build system
        if (StringUtils.startsWith(message, "[maven-release-plugin] prepare release")) {
            val pom = scmClient.getFile("pom.xml", revision)
            return MavenUtils.extractVersion(pom)
        }
        if (StringUtils.startsWith(message, "[maven-release-plugin]")) {
            val pom = scmClient.getFile("pom.xml", revision)
            return MavenUtils.extractVersion(pom)
        }
        return null
    }

}
