package org.neo.gomina.model.user

data class User (
        var id: String,
        var login: String,
        var shortName: String,
        var firstName: String?,
        var lastName: String?,
        var accounts: List<String> = emptyList(),
        var disabled: Boolean
)

interface Users {
    fun getUsers(): List<User>
    fun getUser(userId: String): User?
    fun findForAccount(account: String): User?
    fun addUser(login: String, shortName: String, firstName: String?, lastName: String?, accounts: List<String>): String
    fun updateUser(userId: String, shortName: String, firstName: String?, lastName: String?)
    fun changeAccounts(userId: String, accounts: List<String>)
    fun authenticate(username: String, password: String): User?
    fun resetPassword(userId: String)
    fun changePassword(userId: String, password: String)
    fun enable(userId: String)
    fun disable(userId: String)
}

