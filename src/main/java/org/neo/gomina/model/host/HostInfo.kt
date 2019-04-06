package org.neo.gomina.model.host

import org.neo.gomina.model.version.Version

data class InstanceSshDetails(
        var analyzed: Boolean = false,
        val deployedVersion: String? = null,
        val deployedRevision: String? = null,
        var confCommitted: Boolean? = null,
        var confUpToDate: Boolean? = null,
        var confRevision: String? = null
) {
    val version get() = Version.of(this.deployedVersion, this.deployedRevision)
}

data class HostSshDetails(
        var analyzed: Boolean = false,
        var unexpectedFolders: List<String> = emptyList()
)
