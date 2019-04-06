package org.neo.gomina.model.host

data class Host(
    val host: String,
    val dataCenter: String?,
    val group: String?, // several servers forming a group
    val type: String, // PROD, TEST, etc
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
    fun updateUnexpectedFolders(host: String, unexpectedFolders: List<String>)
}
