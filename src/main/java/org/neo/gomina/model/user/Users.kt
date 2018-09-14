package org.neo.gomina.model.user

data class User (
        var id: String,
        var login: String,
        var firstName: String?,
        var lastName: String?
)

interface Users {
    fun getUsers(): List<User>
    fun getUser(userId: String): User?
}

