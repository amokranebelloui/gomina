package org.neo.gomina.model.dependency

import org.neo.gomina.integration.maven.ArtifactId
import org.neo.gomina.model.version.Version

data class ComponentVersion(val componentId: String, val version: Version)

interface Libraries {
    fun dependencies(componentId: String): List<ArtifactId>
    fun dependents(artifactId: ArtifactId): List<ComponentVersion>
    fun add(componentId: String, version: Version, artifacts: List<ArtifactId>)

}
