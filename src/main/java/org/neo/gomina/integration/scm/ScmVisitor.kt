package org.neo.gomina.integration.scm

import org.apache.commons.lang3.StringUtils
import org.neo.gomina.integration.maven.Artifact
import org.neo.gomina.integration.maven.MavenUtils
import org.neo.gomina.model.component.Component
import org.neo.gomina.model.component.ComponentRepo
import org.neo.gomina.model.component.VersionRelease
import org.neo.gomina.model.scm.Commit
import org.neo.gomina.model.scm.ScmClient
import org.neo.gomina.model.version.Version

interface ComponentScmVisitor {
    fun visitTrunk(commitLog: List<Commit>, latestVersion: Version?, releasedVersion: Version?, changes: Int?)
    fun visitHead(commit: Commit, branch: String, isTrunk: Boolean)
    fun visitVersion(commit: Commit, branch: String, isTrunk: Boolean, versionRelease: VersionRelease)
    fun visitDismissedVersion(artifact: Artifact, dismissedVersion: Version, branch: String, isTrunk: Boolean)
}

fun Component.accept(scmClient: ScmClient, componentRepo: ComponentRepo, visitor: ComponentScmVisitor) {
    this.scm?.let { scm ->

        // Trunk
        val trunk = scmClient.getTrunk() // FIXME Handle Branches
        val log = scmClient.getLog(trunk, "0", 100)

        val latest = log.firstOrNull()?.version
        val latestRevision = log.firstOrNull()?.revision
        val latestVersion: Version? = latest?.takeIf { latest.isNotBlank() }?.let { Version(it, latestRevision) }

        val releasedVersion = releasedVersion(log)
        val changes = commitCountTo(log, releasedVersion?.revision)?.let { it - 1 }

        visitor.visitTrunk(log, latestVersion, releasedVersion, changes)

        val releasedVersions = mutableListOf<String>()
        val processedSnapshot = mutableSetOf<String>()
        val dismissSnapshots = mutableSetOf<Pair<Artifact, Version>>()

        // Commit Log
        val branches = scmClient.getBranches()
        componentRepo.updateBranches(this.id, branches)
        branches.forEach { branch ->
            val isTrunk = scmClient.getTrunk() == branch.name
            val commitLog = scmClient.getLog(branch.name, "0", 100)
            println("-> save commit log: $branch")
            componentRepo.updateCommitLog(this.id, commitLog)

            // Information
            commitLog.firstOrNull()?.let { head -> visitor.visitHead(head, branch.name, isTrunk) }

            // Versions
            commitLog
                    //.filter { c -> c.version != null && Version.isStable(c.version) }
                    .mapNotNull { commit -> commit.version?.let { Triple(
                            MavenUtils.extractArtifactId(scmClient.getFile(branch.name, "pom.xml", commit.revision)),
                            Version(it),
                            commit
                    )}}
                    //.filter { (_, release, _) -> Version.isStable(release) }
                    .forEach { (artifactId, version, commit) ->
                        val versionRelease = VersionRelease(artifactId, version, commit.date, branch.name)
                        if (version.isStable()) {
                            visitor.visitVersion(commit, branch.name, isTrunk, versionRelease)
                            releasedVersions.add(version.version)
                        }
                        else {
                            if (!processedSnapshot.contains(version.version)) {
                                visitor.visitVersion(commit, branch.name, isTrunk, versionRelease)
                                processedSnapshot.add(version.version)
                            }
                            if (releasedVersions.contains(version.withoutSnapshot().version)) {
                                artifactId?.let { dismissSnapshots.add(it to version) }
                            }
                        }
                    }

            dismissSnapshots.forEach { (artifactId, version) ->
                println("Dismiss $version")
                visitor.visitDismissedVersion(artifactId, version, branch.name, isTrunk)
            }
        }
    }

}

private fun releasedVersion(log: List<Commit>): Version? {
    val firstReleaseCommit = log.firstOrNull { it.version != null && Version.isStable(it.version) }
    //val releasedRevision = log.firstOrNull { StringUtils.isNotBlank(it.newVersion) }?.revision
    return if (firstReleaseCommit?.version != null) {
        Version(firstReleaseCommit.version, firstReleaseCommit.revision)
    }
    else null
}

private fun commitCountTo(logEntries: List<Commit>, to: String?): Int? {
    logEntries.map { it.revision }.withIndex().forEach { (count, revision) ->
        if (StringUtils.equals(revision, to)) return count
    }
    return null
}
