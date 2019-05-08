package org.neo.gomina.persistence.model

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.user.User
import org.neo.gomina.model.user.Users
import redis.clients.jedis.JedisPool
import java.nio.charset.StandardCharsets
import java.security.MessageDigest


class RedisUserRepo : Users {
    companion object {
        private val logger = LogManager.getLogger(RedisComponentRepo.javaClass)
    }

    private lateinit var pool: JedisPool

    private val md = MessageDigest.getInstance("SHA-512")

    private val resetPassword = "!!password12"

    @Inject
    private fun initialize(@Named("database.host") host: String, @Named("database.port") port: Int) {
        pool = JedisPool(
                GenericObjectPoolConfig().apply { testOnBorrow = true },
                host, port, 10000, null, 0)
        logger.info("Users Database connected $host $port")
    }

    override fun getUsers(): List<User> {
        pool.resource.use { jedis ->
            val pipe = jedis.pipelined()
            val data = jedis.keys("user:*").map { it.substring(5) to pipe.hgetAll(it) }
            pipe.sync()
            return data.map { (id, data) -> toUser(id, data.get()) }
        }
    }

    override fun getUser(userId: String): User? {
        pool.resource.use { jedis ->
            return jedis.hgetAll("user:$userId")
                    ?.let { toUser(userId, it) }
        }
    }

    override fun findForAccount(account: String): User? {
        // FIXME Optimize database access
        return getUsers().firstOrNull { it.accounts.contains(account) || it.login == account || it.id == account }
    }

    private fun toUser(id: String, map: Map<String, String>) = User(
            id = id,
            login = map["login"] ?: "",
            shortName = map["short_name"] ?: id,
            firstName = map["first_name"],
            lastName = map["last_name"],
            accounts = map["accounts"]?.split(",")?.map { it.trim() } ?: emptyList(),
            disabled = map["disabled"]?.toBoolean() == true
    )

    override fun addUser(userId: String, login: String, shortName: String, firstName: String?, lastName: String?, accounts: List<String>) {
        val passwordHash = md.digest(resetPassword.toByteArray(StandardCharsets.UTF_8)).toString(StandardCharsets.UTF_8)
        pool.resource.use { jedis ->
            return jedis.persist("user:$userId", mapOf(
                    "login" to login,
                    "short_name" to shortName,
                    "first_name" to firstName,
                    "last_name" to lastName,
                    "accounts" to accounts.toStr(),
                    "password_hash" to passwordHash
            ))
        }
    }

    override fun updateUser(userId: String, shortName: String, firstName: String?, lastName: String?) {
        pool.resource.use { jedis ->
            return jedis.persist("user:$userId", mapOf(
                    "short_name" to shortName,
                    "first_name" to firstName,
                    "last_name" to lastName
            ))
        }
    }

    override fun changeAccounts(userId: String, accounts: List<String>) {
        pool.resource.use { jedis ->
            return jedis.persist("user:$userId", mapOf(
                    "accounts" to accounts.toStr()
            ))
        }
    }

    override fun authenticate(userLogin: String, userPassword: String): User? {
        val userPasswordHash = md.digest(userPassword.toByteArray(StandardCharsets.UTF_8)).toString(StandardCharsets.UTF_8)
        pool.resource.use { jedis ->
            // FIXME Optimize database access
            val pipe = jedis.pipelined()
            val data = jedis.keys("user:*").map { it.substring(5) to pipe.hmget(it, "login", "password_hash") }
            pipe.sync()
            val userId = data.map { (id, data) -> data.get().let { Triple(id, it[0], it[1]) } }
                    .filter { (id, login, passwordHash) ->
                        login == userLogin && userPasswordHash == passwordHash
                    }
                    .map { (id, _, _) -> id }
                    .firstOrNull()
            return userId?.let { getUser(it) }
        }
    }

    override fun resetPassword(userId: String) {
        val passwordHash = md.digest(resetPassword.toByteArray(StandardCharsets.UTF_8)).toString(StandardCharsets.UTF_8)
        pool.resource.use { jedis ->
            return jedis.persist("user:$userId", mapOf(
                    "password_hash" to passwordHash
            ))
        }
    }

    override fun changePassword(userId: String, password: String) {
        val passwordHash = md.digest(password.toByteArray(StandardCharsets.UTF_8)).toString(StandardCharsets.UTF_8)
        pool.resource.use { jedis ->
            return jedis.persist("user:$userId", mapOf(
                    "password_hash" to passwordHash
            ))
        }
    }

    override fun enable(userId: String) {
        pool.resource.use { jedis ->
            return jedis.persist("user:$userId", mapOf(
                    "disabled" to false.toString()
            ))
        }
    }

    override fun disable(userId: String) {
        pool.resource.use { jedis ->
            return jedis.persist("user:$userId", mapOf(
                    "disabled" to true.toString()
            ))
        }
    }
}