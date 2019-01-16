package org.neo.gomina.model.host

import org.junit.Test

class HostTest {
    @Test
    fun testUser() {
        val config = Host(host = "host", username = "user", proxyUser = "proxyuser", proxyHost = "proxy", dataCenter = "", group = "", type = "")
        println(config.username + (config.proxyUser?.let { "@$it" } ?: ""))

        val config2 = Host(host = "host", username = "user", dataCenter = "", group = "", type = "")
        println(config2.username + (config2.proxyUser?.let { "@$it" } ?: ""))
    }
}