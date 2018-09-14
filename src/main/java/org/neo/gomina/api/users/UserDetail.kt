package org.neo.gomina.api.users

data class UserDetail (
        val id: String,
        val login: String,
        val firstName: String?,
        val lastName: String?
)
