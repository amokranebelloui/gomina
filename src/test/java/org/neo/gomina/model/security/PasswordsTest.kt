package org.neo.gomina.model.security

import org.apache.commons.codec.binary.Base64
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import java.io.File
import java.util.*

class PasswordsTest {

    @Test
    fun getRealPassword() {
        val passwords = Passwords(File("config/pass.properties"))

        assertThat(String(Base64.encodeBase64("super$\$sec".toByteArray()))).isEqualTo("c3VwZXIkJHNlYw==")
        assertThat(passwords.getRealPassword("@test")).isEqualTo("test")
        assertThat(passwords.getRealPassword("@majortom")).isEqualTo("super$\$sec")
    }

}

fun main(args: Array<String>) {
    val scanner = Scanner(System.`in`)
    print("Password:")
    val password = scanner.next()
    println(String(Base64.encodeBase64(password.toByteArray())))
}