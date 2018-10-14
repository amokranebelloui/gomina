package org.neo.gomina.model.host

import org.neo.gomina.model.inventory.Instance

data class InstanceSshDetails(
        var analyzed: Boolean = false,
        var deployedVersion: String? = null,
        var deployedRevision: String? = null,
        var confCommitted: Boolean? = null,
        var confUpToDate: Boolean? = null,
        var confRevision: String? = null
)

data class HostSshDetails(
        var analyzed: Boolean = false,
        var unexpectedFolders: List<String> = emptyList()
)

interface HostRepo {
    fun getDetails(instance: Instance): InstanceSshDetails?
}