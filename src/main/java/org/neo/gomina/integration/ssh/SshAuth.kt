package org.neo.gomina.integration.ssh

data class SshAuth (
    var username: String,
    var password: String?,
    val sudo: String? = null
)