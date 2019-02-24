package org.neo.gomina.persistence.model

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.user.User
import org.neo.gomina.model.user.Users
import redis.clients.jedis.Jedis
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest



class UsersRepo : Users, AbstractFileRepo() {

    companion object {
        private val logger = LogManager.getLogger(UsersRepo.javaClass)
    }

    @Inject @Named("users.file")
    private lateinit var file: File
    private var users: Map<String, User> = emptyMap()

    fun read(file: File): List<User> {
        return when (file.extension) {
            "yaml" -> yamlMapper.readValue(file)
            "json" -> jsonMapper.readValue(file)
            else -> throw IllegalArgumentException("Format not supported for $file, please use .yaml .json")
        }
    }

    @Inject
    private fun load() {
        users = read(file).associateBy { it.id }
    }

    private fun loadUsers() = read(file)

    override fun getUsers(): List<User> = loadUsers().toList()

    override fun getUser(userId: String): User? = loadUsers().find { it.id == userId }

    override fun findForAccount(account: String): User? =
            loadUsers().find { it.accounts.contains(account) || it.login == account || it.id == account }

    override fun authenticate(username: String, password: String): User? {
        TODO("not implemented")
    }

}

class RedisUserRepo : Users {
    companion object {
        private val logger = LogManager.getLogger(RedisComponentRepo.javaClass)
    }

    private lateinit var jedis: Jedis

    var md = MessageDigest.getInstance("SHA-512")

    @Inject
    private fun initialize(@Named("database.host") host: String, @Named("database.port") port: Int) {
        jedis = Jedis(host, port).also { it.select(0) }
        logger.info("Users Database connected $host $port")
    }

    override fun getUsers(): List<User> {
        val pipe = jedis.pipelined()
        val data = jedis.keys("user:*").map { it.substring(5) to pipe.hgetAll(it) }
        pipe.sync()
        return data.map { (id, data) -> toUser(id, data.get()) }
    }

    override fun getUser(userId: String): User? {
        return jedis.hgetAll("user:$userId")
                ?.let { toUser(userId, it) }
    }

    override fun findForAccount(account: String): User? {
        // FIXME Optimize database access
        return getUsers().firstOrNull { it.accounts.contains(account) || it.login == account || it.id == account }
    }

    private fun toUser(id: String, map: Map<String, String>) = User(
            id = id,
            login = map["login"] ?: "",
            shortName = map["short_name"],
            firstName = map["first_name"],
            lastName = map["last_name"],
            accounts = map["accounts"]?.split(",")?.map { it.trim() } ?: emptyList(),
            disabled = map["disabled"]?.toBoolean() == true
    )

    override fun authenticate(userLogin: String, userPassword: String): User? {
        val userPasswordHash = md.digest(userPassword.toByteArray(StandardCharsets.UTF_8)).toString(StandardCharsets.UTF_8)

        // FIXME Optimize database access
        val pipe = jedis.pipelined()
        val data = jedis.keys("user:*").map { it.substring(5) to pipe.hmget(it, "login", "password_hash") }
        pipe.sync()
        val userId = data.map { (id, data) -> data.get().let { Triple(id, it[0], it[1]) } }
                .filter { (id, login, passwordHash) ->
                    login == userLogin && (passwordHash == null || userPasswordHash == passwordHash)
                }
                .map { (id, _, _) -> id }
                .firstOrNull()
        return userId?.let { getUser(it) }
    }

}