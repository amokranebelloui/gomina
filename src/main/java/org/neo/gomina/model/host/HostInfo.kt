package org.neo.gomina.model.host

import org.neo.gomina.model.inventory.Instance
import org.neo.gomina.model.version.Version

data class InstanceSshDetails(
        var analyzed: Boolean = false,
        val deployedVersion: String? = null,
        val deployedRevision: String? = null,
        var confCommitted: Boolean? = null,
        var confUpToDate: Boolean? = null,
        var confRevision: String? = null
) {
    val version get() = if (this.deployedVersion?.isNotEmpty() == true) Version(this.deployedVersion, this.deployedRevision) else null
}

data class HostSshDetails(
        var analyzed: Boolean = false,
        var unexpectedFolders: List<String> = emptyList()
)

interface HostRepo {
    fun getDetails(instance: Instance): InstanceSshDetails?
}