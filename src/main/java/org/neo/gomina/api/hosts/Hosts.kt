package org.neo.gomina.api.hosts

data class HostDetail(
        val host: String,
        val dataCenter: String?,

        val username: String,
        val passwordAlias: String? = null,
        val sudo: String? = null,

        val unexpected: List<String>
)