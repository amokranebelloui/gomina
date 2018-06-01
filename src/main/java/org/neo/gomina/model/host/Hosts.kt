package org.neo.gomina.model.host

data class Host(
    val host: String,
    val dataCenter: String?,
    val username: String,
    val passwordAlias: String? = null,
    val sudo: String? = null
)

interface Hosts {
    fun getHosts(): List<Host>
    fun getHost(host: String): Host?
}
