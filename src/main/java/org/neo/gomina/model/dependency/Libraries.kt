package org.neo.gomina.model.dependency

import org.neo.gomina.integration.maven.ArtifactId
import org.neo.gomina.model.version.Version

data class LibraryVersions(val artifactId: ArtifactId, val versions: List<Version>)

data class ComponentVersion(val componentId: String, val version: Version)

interface Libraries {
    fun libraries(): List<LibraryVersions>
    fun library(artifactId: ArtifactId): Map<Version, List<ComponentVersion>>
    fun forComponent(componentId: String, version: Version): List<ArtifactId>
    fun addArtifactId(artifactId: String, version: Version)
    fun addUsage(componentId: String, version: Version, artifacts: List<ArtifactId>)
    fun cleanSnapshotVersions(componentId: String)

}
