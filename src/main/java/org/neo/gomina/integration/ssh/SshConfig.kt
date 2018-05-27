package org.neo.gomina.integration.ssh

data class Host (
    val host: String,
    val username: String,
    val passwordAlias: String? = null,
    val sudo: String? = null
)

data class SshConfig (val hosts: List<Host> = emptyList())