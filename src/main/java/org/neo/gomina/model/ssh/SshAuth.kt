package org.neo.gomina.model.ssh

data class SshAuth (
    var username: String,
    var password: String?,
    val sudo: String? = null
)