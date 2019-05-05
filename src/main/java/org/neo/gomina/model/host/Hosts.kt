package org.neo.gomina.model.host

data class Host(
    val host: String,
    val dataCenter: String?,
    val group: String?, // several servers forming a group
    val type: String, // PROD, TEST, etc
    val osFamily: String? = null, // windows, linux, macos, dummy, etc.
    val os: String? = null, // Windows 10, RedHat 7, macOS 10.12 Sierra, etc.
    val tags: List<String> = emptyList(),
    val username: String?,
    val passwordAlias: String? = null,
    val proxyUser: String? = null,
    val proxyHost: String? = null,
    val sudo: String? = null,

    var unexpectedFolders: List<String> = emptyList()
)

interface Hosts {
    fun getHosts(): List<Host>
    fun getHost(host: String): Host?
    fun addHost(hostId: String)
    fun updateHost(hostId: String, dataCenter: String?, group: String?, type: String, osFamily: String?, os: String?, tags: List<String>)
    fun updateConnectivity(hostId: String, username: String?, passwordAlias: String?, proxyHost: String?, proxyUser: String?, sudo: String?)
    fun updateUnexpectedFolders(host: String, unexpectedFolders: List<String>)
}
