package org.neo.gomina.model.dependency

import org.neo.gomina.integration.maven.Artifact
import org.neo.gomina.model.version.Version

data class VersionUsage(val version: Version, val dependents: Int)

data class LibraryVersions(val artifactId: Artifact, val versions: List<VersionUsage>)

data class ComponentVersion(val componentId: String, val version: Version)

interface Libraries {
    fun libraries(): List<LibraryVersions>
    fun library(artifact: Artifact): Map<Version, List<ComponentVersion>>
    fun forComponent(componentId: String, version: Version): List<Artifact>
    fun addArtifact(artifact: Artifact, version: Version)
    fun addUsage(componentId: String, version: Version, artifacts: List<Artifact>)
    fun dismissSnapshotVersion(artifact: Artifact, version: Version)

}
