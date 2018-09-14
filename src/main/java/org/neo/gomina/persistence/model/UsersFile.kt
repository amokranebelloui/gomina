package org.neo.gomina.persistence.model

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.logging.log4j.LogManager
import org.neo.gomina.model.user.User
import org.neo.gomina.model.user.Users
import java.io.File

class UsersFile : Users, AbstractFileRepo() {

    companion object {
        private val logger = LogManager.getLogger(UsersFile.javaClass)
    }

    @Inject @Named("users.file")
    private lateinit var file: File

    fun read(file: File): List<User> {
        return when (file.extension) {
            "yaml" -> yamlMapper.readValue(file)
            "json" -> jsonMapper.readValue(file)
            else -> throw IllegalArgumentException("Format not supported for $file, please use .yaml .json")
        }
    }

    override fun getUsers(): List<User> = read(file)

    override fun getUser(userId: String): User? = read(file).find { it.id == userId }


}