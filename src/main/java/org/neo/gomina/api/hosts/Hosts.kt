package org.neo.gomina.api.hosts

data class HostDetail(
        val host: String,
        val dataCenter: String?,

        val unexpected: List<String>
)