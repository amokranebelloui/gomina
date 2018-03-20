package org.neo.gomina.model.security

import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class Passwords {

    internal var props = Properties()

    @Inject
    constructor(@Named("passwords") file: File) {
        try {
            logger.info("Passwords from {}", file)
            props.load(FileInputStream(file))
            logger.info("{} passwords loaded", props.size)
        }
        catch (e: IOException) {
            logger.error("Cannot load passwords")
        }
    }

    fun getRealPassword(alias: String): String? {
        if (StringUtils.isNotBlank(alias)) {
            val encoded = props.getProperty(alias)
            return String(Base64.decodeBase64(encoded.toByteArray()))
        }
        return null
    }

    companion object {
        private val logger = LogManager.getLogger(Passwords::class.java)
    }

}