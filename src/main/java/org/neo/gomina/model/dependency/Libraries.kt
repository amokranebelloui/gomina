package org.neo.gomina.model.dependency

import org.neo.gomina.integration.maven.ArtifactId
import org.neo.gomina.model.version.Version

data class ComponentVersion(val componentId: String, val version: Version)

interface Libraries {
    fun libraries(): Map<ArtifactId, List<Version>>
    fun library(artifactId: ArtifactId): Map<Version, List<ComponentVersion>>
    fun usedByComponent(componentId: String): List<ArtifactId>
    fun componentsUsing(artifactId: ArtifactId): List<ComponentVersion>
    fun add(componentId: String, version: Version, artifacts: List<ArtifactId>)
    fun addArtifactId(artifactId: String?, version: Version)
    fun changeArtifactId(artifactId: String?, oldArtifactId: String?, version: Version?)

}
