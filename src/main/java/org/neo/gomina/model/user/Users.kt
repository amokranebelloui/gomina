package org.neo.gomina.model.user

data class User (
        var id: String,
        var login: String,
        var shortName: String?,
        var firstName: String?,
        var lastName: String?,
        var accounts: List<String> = emptyList()
)

interface Users {
    fun getUsers(): List<User>
    fun getUser(userId: String): User?
    fun findForAccount(account: String): User?
}

